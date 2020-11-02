package de.zweelo.gateway.service.impl;

import de.zweelo.gateway.domain.*;
import de.zweelo.gateway.domain.enumeration.RequestStatus;
import de.zweelo.gateway.domain.enumeration.Session;
import de.zweelo.gateway.repository.DriverRepository;
import de.zweelo.gateway.repository.JourneyPlanRepository;
import de.zweelo.gateway.repository.RideManagementRepository;
import de.zweelo.gateway.security.AuthoritiesConstants;
import de.zweelo.gateway.service.*;
import de.zweelo.gateway.repository.RideRequestRepository;
import de.zweelo.gateway.service.dto.*;
import de.zweelo.gateway.service.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

/**
 * Service Implementation for managing {@link RideRequest}.
 */
@Service
@Transactional
public class RideRequestServiceImpl implements RideRequestService {

    private final Logger log = LoggerFactory.getLogger(RideRequestServiceImpl.class);

    private final double minPickupTime = 14; // as 2pm

    private final double maxDropoffTime = 10; // as 10am

    private final RideRequestRepository rideRequestRepository;

    private final LocationService locationService;

    private final RideFrequencyService rideFrequencyService;

    private final RideRequestFrequencyRelationshipService rideRequestFrequencyRelationshipService;

    private final RideRequestMapper rideRequestMapper;

    private final UserService userService;

    private final UserMapper userMapper;

    private final PassengerService passengerService;

    private final PassengerMapper passengerMapper;

    private final ChildService childService;

    private final ChildMapper childMapper;

    private final RideManagementRepository rideManagementRepository;

    private final RideManagementMapper rideManagementMapper;

    private final JourneyPlanService journeyPlanService;

    private final RideManagementService rideManagementService;

    private final JourneyPlanRepository journeyPlanRepository;

    private final DriverRepository driverRepository;

    private final DriverMapper driverMapper;

    private final JourneyEventService journeyEventService;


    private final RideRequestFrequencyRelationshipService requestFrequencyRelationshipService;

    public RideRequestServiceImpl(RideRequestRepository rideRequestRepository, LocationService locationService,
                                  RideFrequencyService rideFrequencyService,
                                  RideRequestFrequencyRelationshipService rideRequestFrequencyRelationshipService,
                                  RideRequestMapper rideRequestMapper, UserService userService, UserMapper userMapper,
                                  PassengerService passengerService, PassengerMapper passengerMapper,
                                  ChildService childService, ChildMapper childMapper,
                                  RideManagementRepository rideManagementRepository, RideManagementMapper rideManagementMapper,
                                  JourneyPlanService journeyPlanService,
                                  @Lazy RideManagementService rideManagementService,
                                  JourneyPlanRepository journeyPlanRepository, DriverRepository driverRepository, DriverMapper driverMapper, JourneyEventService journeyEventService, @Lazy RideRequestFrequencyRelationshipService requestFrequencyRelationshipService) {
        this.rideRequestRepository = rideRequestRepository;
        this.locationService = locationService;
        this.rideFrequencyService = rideFrequencyService;
        this.rideRequestFrequencyRelationshipService = rideRequestFrequencyRelationshipService;
        this.rideRequestMapper = rideRequestMapper;
        this.userService = userService;
        this.userMapper = userMapper;
        this.passengerService = passengerService;
        this.passengerMapper = passengerMapper;
        this.childService = childService;
        this.childMapper = childMapper;
        this.rideManagementRepository = rideManagementRepository;
        this.rideManagementMapper = rideManagementMapper;
        this.journeyPlanService = journeyPlanService;
        this.rideManagementService = rideManagementService;
        this.journeyPlanRepository = journeyPlanRepository;
        this.driverRepository = driverRepository;
        this.driverMapper = driverMapper;
        this.journeyEventService = journeyEventService;
        this.requestFrequencyRelationshipService = requestFrequencyRelationshipService;
    }

    /**
     * Save a rideRequest.
     *
     * @param rideRequestDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public RideRequestDTO save(RideRequestDTO rideRequestDTO) {
        log.debug("Request to save RideRequest : {}", rideRequestDTO);

        /*
          Make sure Parent(Passenger) is real
          */
        User user = userService.getUserWithAuthorities(rideRequestDTO.getUserId())
            .orElseThrow(() -> new RuntimeException("Unknown user id " + rideRequestDTO.getUserId()));
        PassengerDTO passengerDTO = passengerService.findByUser(user)
            .orElseThrow(() -> new RuntimeException("Invalid user"));

        /*
         * Make sure Child is of Passenger & not of someone else
         * */
        ChildDTO childDTO = childService.findOne(rideRequestDTO.getChildId())
            .orElseThrow(() -> new RuntimeException("Invalid child"));

        if(!childDTO.getUserId().equals(passengerDTO.getUserId()))
            throw new RuntimeException("Invalid child and parent relationship");

        /*
         * Create Pickup Location & Drop Location, enter in DB
         * */
        if(rideRequestDTO.getPickupLocation() == null && rideRequestDTO.getDropLocation() == null)
            throw new RuntimeException("Pickup or Drop location is required.");

        if(rideRequestDTO.getPickupLocation() != null) {
            LocationDTO pickupLocationDTO = locationService.save(rideRequestDTO.getPickupLocation());
            if (pickupLocationDTO != null && pickupLocationDTO.getId() != null)
                rideRequestDTO.setPickupLocationId(pickupLocationDTO.getId());
        }

        if(rideRequestDTO.getDropLocation() != null) {
            LocationDTO dropLocationDTO = locationService.save(rideRequestDTO.getDropLocation());
            if (dropLocationDTO != null && dropLocationDTO.getId() != null)
                rideRequestDTO.setDropLocationId(dropLocationDTO.getId());
        }
        /*
         * Fetch LoggedIn user info
         * Only Admin can approve / reject request.
         * IF User updates a request its back to pending state
         *
         * */
        UserDTO userDTO = userService.getLoggedInUserDTO();
        if(!userDTO.getAuthorities().contains(AuthoritiesConstants.ADMIN)) {
            rideRequestDTO.setRequestStatus(RequestStatus.RAISED);
        }

        final RideRequest rideRequest = rideRequestRepository.save(rideRequestMapper.toEntity(rideRequestDTO));
        if(rideRequest == null || rideRequest.getId() == null)
            throw new RuntimeException("Unable to create Ride request " + rideRequestDTO.toString() +", Rolling back...");

        /*
         * Create Frequency
         * */
        List<RideFrequencyDTO> rideFrequencyDTOS;
        if(rideRequestDTO.getFrequencies() != null && rideRequestDTO.getFrequencies().size() > 0) {
            rideFrequencyDTOS = rideRequestDTO.getFrequencies()
                .stream().map(rideFrequencyDTO -> {
                    RideFrequencyDTO dto = rideFrequencyService.save(rideFrequencyDTO);
                    if (dto != null && dto.getId() != null)
                        return dto;
                    else
                        throw new RuntimeException("Unable to create Frequency " + rideFrequencyDTO.toString() + ", Rolling back...");
                }).collect(Collectors.toList());
        } else {
            throw new RuntimeException("Frequencies is required.");
        }

        /*
         * Save FrequencyId & RideRequestId in
         * RIDE_REQ_FREQ_RELATIONSHIP TABLE
         * */
        if(rideFrequencyDTOS != null && rideFrequencyDTOS.size() > 0) {
            rideFrequencyDTOS.stream().forEach(rideFrequencyDTO -> {
                RideRequestFrequencyRelationshipDTO dto = rideRequestFrequencyRelationshipService.save(
                    new RideRequestFrequencyRelationshipDTO(rideRequest.getId(), rideFrequencyDTO.getId()));
                if (dto == null || dto.getId() == null)
                    throw new RuntimeException("Unable to create relationship of rideRequest=" + rideRequest.getId()
                        + ", rideFrequency=" + rideFrequencyDTO.getId());
            });
        }

        return rideRequestMapper.toDto(rideRequest);
    }

    /**
     * Save a rideRequest from Web.
     *
     * @param rideRequestDTO the entity to save.
     * @return the persisted entity.
     */
    @Override
    public RideRequestDTO saveFromWeb(RideRequestDTO rideRequestDTO) {
        log.debug("Request to save RideRequest from web : {}", rideRequestDTO);

        /*
          Make sure Parent(Passenger) is real
          */
        User user = userService.getUserWithAuthorities(rideRequestDTO.getUserId())
            .orElseThrow(() -> new RuntimeException("Unknown user id " + rideRequestDTO.getUserId()));
        PassengerDTO passengerDTO = passengerService.findByUser(user)
            .orElseThrow(() -> new RuntimeException("Invalid user"));

        /*
         * Make sure Child is of Passenger & not of someone else
         * */
        ChildDTO childDTO = childService.findOne(rideRequestDTO.getChildId())
            .orElseThrow(() -> new RuntimeException("Invalid child"));

        if(!childDTO.getUserId().equals(passengerDTO.getUserId()))
            throw new RuntimeException("Invalid child and parent relationship");

        /*
         * Create Pickup Location & Drop Location, enter in DB
         * */
        if(rideRequestDTO.getPickupLocation() == null && rideRequestDTO.getDropLocation() == null)
            throw new RuntimeException("Pickup or Drop location is required.");

        if(rideRequestDTO.getPickupLocation() != null) {
            LocationDTO pickupLocationDTO = locationService.save(rideRequestDTO.getPickupLocation());
            if (pickupLocationDTO != null && pickupLocationDTO.getId() != null)
                rideRequestDTO.setPickupLocationId(pickupLocationDTO.getId());
        }

        if(rideRequestDTO.getDropLocation() != null) {
            LocationDTO dropLocationDTO = locationService.save(rideRequestDTO.getDropLocation());
            if (dropLocationDTO != null && dropLocationDTO.getId() != null)
                rideRequestDTO.setDropLocationId(dropLocationDTO.getId());
        }
        /*
         * Fetch LoggedIn user info
         * Only Admin can approve / reject request.
         * IF User updates a request its back to pending state
         *
         * */
        UserDTO userDTO = userService.getLoggedInUserDTO();
        if(userDTO.getAuthorities().contains(AuthoritiesConstants.ADMIN)) {
            rideRequestDTO.setRequestStatus(RequestStatus.RAISED);
        }

        final RideRequest rideRequest = rideRequestRepository.save(rideRequestMapper.toEntity(rideRequestDTO));
        if(rideRequest == null || rideRequest.getId() == null)
            throw new RuntimeException("Unable to create Ride request " + rideRequestDTO.toString() +", Rolling back...");

        /*
         * Create Frequency
         * */
        List<RideFrequencyDTO> rideFrequencyDTOS;
        if(rideRequestDTO.getFrequencies() != null && rideRequestDTO.getFrequencies().size() > 0) {
            rideFrequencyDTOS = rideRequestDTO.getFrequencies()
                .stream().map(rideFrequencyDTO -> {
                    RideFrequencyDTO dto = rideFrequencyService.save(rideFrequencyDTO);
                    if (dto != null && dto.getId() != null)
                        return dto;
                    else
                        throw new RuntimeException("Unable to create Frequency " + rideFrequencyDTO.toString() + ", Rolling back...");
                }).collect(Collectors.toList());
        } else {
            throw new RuntimeException("Frequencies is required.");
        }

        /*
         * Save FrequencyId & RideRequestId in
         * RIDE_REQ_FREQ_RELATIONSHIP TABLE
         * */
        if(rideFrequencyDTOS != null && rideFrequencyDTOS.size() > 0) {
            rideFrequencyDTOS.stream().forEach(rideFrequencyDTO -> {
                RideRequestFrequencyRelationshipDTO dto = rideRequestFrequencyRelationshipService.save(
                    new RideRequestFrequencyRelationshipDTO(rideRequest.getId(), rideFrequencyDTO.getId()));
                if (dto == null || dto.getId() == null)
                    throw new RuntimeException("Unable to create relationship of rideRequest=" + rideRequest.getId()
                        + ", rideFrequency=" + rideFrequencyDTO.getId());
            });
        }

        return rideRequestMapper.toDto(rideRequest);
    }


    /**
     * Get all the rideRequests.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RideRequestDTO> findAll(Pageable pageable) {
        log.info("Request to get all RideRequests");
        /*
         * Fetch LoggedIn user info
         * */
        UserDTO userDTO = userService.getLoggedInUserDTO();

        if(!userDTO.getAuthorities().isEmpty() && userDTO.getAuthorities().contains(AuthoritiesConstants.PASSENGER)
        && !userDTO.getAuthorities().contains(AuthoritiesConstants.ADMIN)) {
            /*
             * If user is (PARENT a.k.a passenger) & not ADMIN then only send his Ride Requests
             * */
            return toDTOs(rideRequestRepository.findAllByUser(userMapper.userDTOToUser(userDTO), pageable));
        } else {
            return toDTOs(rideRequestRepository.findAll(pageable));
        }
    }

    /**
     * This service is Used in Angular for Admin Dashboard Screen
     * */
    @Override
    public Page<RideRequestDTO> findByStatus(RequestStatus requestStatus, Pageable pageable) {
        /*
         * Fetch LoggedIn user info
         * */
        UserDTO userDTO = userService.getLoggedInUserDTO();

        if(userDTO.getAuthorities().contains(AuthoritiesConstants.ADMIN)) {
           return toDTOs(rideRequestRepository.findAllByRequestStatus(requestStatus, pageable));
        } else {
            return Page.empty();
        }
    }

    /**
     * This service is Used in Angular for Admin Dashboard Screen
     * */
    @Override
    public List<RideRequestDTO> findAllJourneys(LocalDate date) {
        log.info("Request to get all RideRequests for tomorrow");
        /*
         * Fetch LoggedIn user info
         * */
        UserDTO userDTO = userService.getLoggedInUserDTO();
        if(userDTO.getAuthorities().contains(AuthoritiesConstants.ADMIN)) {
            return rideRequestRepository.findAll(date)
                .stream()
                .map(rideRequest -> {
                    /*
                     * Fetch all necessary dto
                     * */
                    RideRequestDTO rideRequestDTO =  toDTO(rideRequest);

                    /*
                     * Check if Journey is already planned or not.
                     * */
                    try {
                        JourneyPlanDTO journeyPlanDTO = journeyPlanService.findByDateAndRideRequestId(date, rideRequestDTO.getId());
                        if(journeyPlanDTO != null)
                            rideRequestDTO.setJourneyPlanDTO(journeyPlanDTO);
                    } catch (Exception exc) {
                        log.info(exc.toString());
                    }

                    return rideRequestDTO;
                })
                .filter(rideRequestDTO -> rideMatchesDateAndFrequency(rideRequestDTO, date))
                .collect(Collectors.toList());
        } else {
            throw new AccessDeniedException("");
        }
    }

    /**
     * Check if RideRequest Frequency DAY matches the Date's Day
     * For eg: If Ride Request has frequency MONDAY, WEDNESDAY & date(from param) is TUESDAY then,
     * it should not be shown.
     * */
    private boolean rideMatchesDateAndFrequency(RideRequestDTO rideRequestDTO, LocalDate date) {
        if (rideRequestDTO == null || date == null)
            return false;

        log.info("rideMatchesDateAndFrequency DATE: {} | rideRequestDTO:{}",date.toString(), rideRequestDTO);
        for (RideFrequencyDTO rideFrequencyDTO: rideRequestDTO.getFrequencies()) {
            if(rideFrequencyDTO.getDay().toString().equals(date.getDayOfWeek().name())) {
                Set<RideFrequencyDTO> frequencies = new HashSet<>();
                frequencies.add(rideFrequencyDTO);
                rideRequestDTO.setFrequencies(frequencies);
                return true;
            }
        }
        return false;
    }

    /**
     * API used by Mobile app - Driver Today's Trip
     * */
    @Override
    public List<RideRequestDTO> findAllRideRequestByUserAndDate(LocalDate date) {
        log.info("findAllRideRequestByUserAndDate {}", date);
        UserDTO userDTO = userService.getLoggedInUserDTO();
        return findAllRideRequestByUserAndDate(userDTO.getId(), date);
    }

    /**
     * API used by Web Admin - Tracking Screen
     * */
    @Override
    public List<RideRequestDTO> findAllRideRequestByUserAndDate(long userId, LocalDate date) {
        log.info("findAllRideRequestByUserAndDate {} {}", userId, date);

        List<JourneyPlanDTO> journeyPlanDTOS = journeyPlanService.findAllByUserAndDate(userId, date);
        if(journeyPlanDTOS != null && journeyPlanDTOS.size() > 0) {
            return journeyPlanDTOS
                .stream()
                .map(journeyPlanDTO -> {
                    RideRequestDTO rideRequestDTO = findOne(journeyPlanDTO.getRideRequestId())
                        .orElseThrow(() -> new RuntimeException("Invalid Ride Request id " + journeyPlanDTO.getRideRequestId()));
                    rideRequestDTO.setJourneyPlanDTO(journeyPlanDTO);
                    return rideRequestDTO;
                }).filter(rideRequestDTO -> rideMatchesDateAndFrequency(rideRequestDTO, date))
                .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    /**
     * API used by Mobile app - Driver Upcoming & Past Journey
     * */
    @Override
    public DriverJourneyDTO findAllJourneysByDriver() {
        log.info("Find all journey by driver");

        UserDTO userDTO = userService.getLoggedInUserDTO();
        log.info(userDTO.toString());

        DriverJourneyDTO driverJourneyDTO = new DriverJourneyDTO();

        List<JourneyPlanDTO> journeyPlanDTOS = journeyPlanService.findAllByUser(userDTO.getId());
        if(journeyPlanDTOS != null && journeyPlanDTOS.size() > 0) {
            journeyPlanDTOS
            .stream()
            .map(journeyPlanDTO -> {
                RideRequestDTO rideRequestDTO = findOne(journeyPlanDTO.getRideRequestId())
                    .orElseThrow(() -> new RuntimeException("Invalid Ride Request id " + journeyPlanDTO.getRideRequestId()));
                rideRequestDTO.setJourneyPlanDTO(journeyPlanDTO);
                return rideRequestDTO;
            }).forEach(rideRequestDTO -> {
                // Check if journey is completed or not
                if (rideRequestDTO.getJourneyPlanDTO() != null
                    && rideRequestDTO.getJourneyPlanDTO().getJourneyDate().toEpochDay() < LocalDate.now().toEpochDay()) {
                        driverJourneyDTO.getPastJourneys().add(rideRequestDTO);
                } else {
                    driverJourneyDTO.getUpcomingJourneys().add(rideRequestDTO);
                }
            });
        }
        return driverJourneyDTO;
    }

    private RideRequestDTO toDTO(RideRequest rideRequest) {
        RideRequestDTO rideRequestDTO = rideRequestMapper.toDto(rideRequest);

        /*
         * Fetch ALL FrequencyID from RIDE_REQ_FREQ_RELATIONSHIP table
         * */
        List<RideRequestFrequencyRelationshipDTO> relationships = rideRequestFrequencyRelationshipService.findAllByRideRequestId(rideRequestDTO.getId());

        /*
         * Fetch ALL FrequenciesDTO
         * */
        Set<RideFrequencyDTO> rideFrequencyDTOS = relationships
            .stream()
            .map(relationship -> {
                /*
                 * fetch rideFrequencyDTO by rideFrequencyID
                 * */
                Optional<RideFrequencyDTO> optionalRideFrequencyDTO =
                    rideFrequencyService.findOne(relationship.getRideFrequencyId());
                if(optionalRideFrequencyDTO.isPresent()) {
                    return optionalRideFrequencyDTO.get();
                } else {
                    throw new RuntimeException("Unable to fetch Frequency id=" + relationship.getRideFrequencyId());
                }
            })
            .collect(Collectors.toSet());
        rideRequestDTO.setFrequencies(rideFrequencyDTOS);

        try {
            /*
             * Fetch Passenger(User) Object
             * */
            User user = userService.getUserWithAuthoritiesByLogin(rideRequestDTO.getUserLogin())
                .orElseThrow(() -> new RuntimeException("Unable to find user"));

            PassengerDTO passengerDTO = passengerService.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Unable to find parent = " + user.getId() + " where ride_request_id =" + rideRequestDTO.getId()));
            rideRequestDTO.setPassengerDTO(passengerDTO);
        } catch (Exception exc) {
            log.error(exc.toString());
        }

        /*
         * If ride request is approved or rejected fetch the latest RequestManagement State
         * */
        if(rideRequestDTO.getRequestStatus() != RequestStatus.RAISED) {
            RideManagement rideManagement = rideManagementRepository.findFirstByRideRequestOrderByTimestampDesc(rideRequest)
                .orElseThrow(() -> new RuntimeException("Unable to fetch ride Management!"));
            rideRequestDTO.setRideManagementDTO(rideManagementMapper.toDto(rideManagement));
        }

        return rideRequestDTO;
    }

    private Page<RideRequestDTO> toDTOs(Page<RideRequest> rideRequests) {
        return rideRequests
            .map(this::toDTO);
    }


    /**
     * Get one rideRequest by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RideRequestDTO> findOne(Long id) {
        log.info("Request to get RideRequest : {}", id);
        return rideRequestRepository.findById(id)
            .map(this::toDTO);
    }

    @Override
    public long findPassengerUserIdByJourneyPlanId(Long journeyPlanId) {
        JourneyPlanDTO journeyPlanDTO = journeyPlanService.findOne(journeyPlanId).orElseThrow(() -> new RuntimeException("Unable to find journey Plan by id " + journeyPlanId));
        RideRequest rideRequest = rideRequestRepository.findById(journeyPlanDTO.getRideRequestId()).orElseThrow(() -> new RuntimeException("Unable to find ride request by " + journeyPlanDTO));
        return rideRequest.getUser().getId();

    }

    /*
     * Delete the rideRequest by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.info("Request to delete RideRequest : {}", id);
        rideRequestRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        log.info("Delete All Ride Request by user id {}", userId);

        /*
         * Delete all dependencies before deleting rideRequest.
         * */
        rideRequestRepository.findAllByUserId(userId)
            .forEach(rideRequest -> {

                // 1. Delete RideRequestFrequencyRelationship by RideRequestId
                rideRequestFrequencyRelationshipService.deleteByRideRequestId(rideRequest.getId());

                // 2. Delete RideManagement by RideRequestId
                rideManagementService.deleteByRideRequestId(rideRequest.getId());

                // 3. Delete Journey Plan
                journeyPlanService.deleteByRideRequestId(rideRequest.getId());

                // Delete Ride Request
                delete(rideRequest.getId());

                // Delete both Pick & Drop locations
                locationService.delete(rideRequest.getPickupLocation().getId());
                locationService.delete(rideRequest.getDropLocation().getId());
            });
    }

    /**
     * This service is used by Mobile app - to show ride requests by requestStatus for logged-in passenger(user)
     * */
    @Override
    public List<RideRequestDTO> findAllRideRequestByUserAndRequestStatus(RequestStatus requestStatus) {

        List<RideRequestDTO> rideRequestDTOS = new ArrayList<>();
        /*
         * Fetch LoggedIn user info
         * */
        UserDTO userDTO = userService.getLoggedInUserDTO();

        // to check if logged-in user is passenger or not!
        if(userDTO.getAuthorities().contains(AuthoritiesConstants.PASSENGER)) {
            List<RideRequest> rideRequests = rideRequestRepository.findAllByUserAndRequestStatus(userMapper.userDTOToUser(userDTO), requestStatus);
            if (!rideRequests.isEmpty()) {
                rideRequests.forEach(rideRequest -> {
                    rideRequestDTOS.add(this.toDTO(rideRequest));
                });
            }
        }
        return rideRequestDTOS;
    }

    /**
     * This service is used by Mobile app - to show ride requests by date for logged-in passenger(user)
     * */
    @Override
    public List<RideRequestDTO> findAllByUserAndDate(LocalDate date) {

        List<RideRequestDTO> rideRequestDTOS = new ArrayList<>();
        /*
         * Fetch LoggedIn user info
         * */
        UserDTO userDTO = userService.getLoggedInUserDTO();

        // to check if logged-in user is passenger or not!
        if(userDTO.getAuthorities().contains(AuthoritiesConstants.PASSENGER)) {
            List<RideRequest> rideRequests = rideRequestRepository.findAllByUserAndDate(userMapper.userDTOToUser(userDTO), date);
            if (!rideRequests.isEmpty()) {
                rideRequests.forEach(rideRequest -> {
                    rideRequestDTOS.add(this.toDTO(rideRequest));
                });
            }
        }

        return rideRequestDTOS;
    }

    /**
     * This service is used by Mobile app - to show ride requests by date & requestStatus for logged-in passenger(user)
     * */
    @Override
    public List<RideRequestDTO> findAllRideRequestByUserAndDateAndRequestStatus(RequestStatus requestStatus, LocalDate date) {

        log.info("Request to get Ride requests with requestStatus {} and date {} ", requestStatus, date);

        List<RideRequestDTO> rideRequestDTOS = new ArrayList<>();
        /*
         * Fetch LoggedIn user info
         * */
        UserDTO userDTO = userService.getLoggedInUserDTO();

        // to check if logged-in user is passenger or not!
        if(userDTO.getAuthorities().contains(AuthoritiesConstants.PASSENGER)) {
            List<RideRequest> rideRequests = rideRequestRepository.findAllByUserAndDateAndRequestStatus(userMapper.userDTOToUser(userDTO), requestStatus, date);
            if (!rideRequests.isEmpty()) {
                rideRequests.forEach(rideRequest -> {

                    //check if day is there in rideRequestFrequencies!
                    List<RideRequestFrequencyRelationshipDTO> list = rideRequestFrequencyRelationshipService.findAllByRideRequestId(rideRequest.getId());

                    // to get unique rideFrequency Ids
                    Set<Long> rideFrequencyIds = new HashSet<>();
                    if (!list.isEmpty()) {
                        list.forEach(rideRequestFrequencyRelationshipDTO -> {
                            rideFrequencyIds.add(rideRequestFrequencyRelationshipDTO.getRideFrequencyId());
                        });
                    }

                    // get rideFrequencies and store it to set
                    if (!rideFrequencyIds.isEmpty()) {
                        rideFrequencyIds.forEach(rideFrequencyId -> {
                            rideFrequencyService.findOne(rideFrequencyId).ifPresent(rideFrequencyDTO -> {
                                // check if day of date in parameter, present in rideFrequencies!
                                if (rideFrequencyDTO.getDay().name().equalsIgnoreCase(date.getDayOfWeek().toString())) {
                                    rideRequestDTOS.add(this.toDTO(rideRequest));
                                }
                            });
                        });
                    }

                });
            }
        }
        return rideRequestDTOS;
    }

    /**
     * This service is used by Mobile app - to show ride requests by date, childName & requestStatus for logged-in passenger(user)
     * */
    @Override
    public List<RideRequestDTO> findAllRideRequestByUserAndDateAndNameAndRequestStatus(RequestStatus requestStatus, String childName, LocalDate date) {

        log.info("Request to get Ride requests with requestStatus {} and childName {} and date {} ", requestStatus, childName, date);

        List<RideRequestDTO> rideRequestDTOS = new ArrayList<>();
        /*
         * Fetch LoggedIn user info
         * */
        UserDTO userDTO = userService.getLoggedInUserDTO();

        // to check if logged-in user is passenger or not!
        if(userDTO.getAuthorities().contains(AuthoritiesConstants.PASSENGER)) {
            List<RideRequest> rideRequests = rideRequestRepository.findAllByUserAndDateAndNameAndRequestStatus(userMapper.userDTOToUser(userDTO), childName, requestStatus, date);
            if (!rideRequests.isEmpty()) {
                rideRequests.forEach(rideRequest -> {

                    //check if day is there in rideRequestFrequencies!
                    List<RideRequestFrequencyRelationshipDTO> list = rideRequestFrequencyRelationshipService.findAllByRideRequestId(rideRequest.getId());

                    // to get unique rideFrequency Ids
                    Set<Long> rideFrequencyIds = new HashSet<>();
                    if (!list.isEmpty()) {
                        list.forEach(rideRequestFrequencyRelationshipDTO -> {
                            rideFrequencyIds.add(rideRequestFrequencyRelationshipDTO.getRideFrequencyId());
                        });
                    }

                    // get rideFrequencies and store it to set
                    if (!rideFrequencyIds.isEmpty()) {
                        rideFrequencyIds.forEach(rideFrequencyId -> {
                            rideFrequencyService.findOne(rideFrequencyId).ifPresent(rideFrequencyDTO -> {
                                // check if day of date in parameter, present in rideFrequencies!
                                if (rideFrequencyDTO.getDay().name().equalsIgnoreCase(date.getDayOfWeek().toString())) {
                                    rideRequestDTOS.add(this.toDTO(rideRequest));
                                }
                            });
                        });
                    }

                });
            }
        }
        return rideRequestDTOS;
    }

    /**
     * This service is used by Mobile app - to show ride requests by date, childName & requestStatus for logged-in passenger(user) with journeyEvent
     * */
    @Override
    public List<RideRequestDTOv2> findAllRideRequestByUserAndDateAndNameAndRequestStatusV2(RequestStatus requestStatus, String childName, LocalDate date) {

        log.info("Request to get Ride requests with requestStatus {} and childName {} and date {} ", requestStatus, childName, date);

        List<RideRequestDTOv2> rideRequestDTOSv2 = new ArrayList<>();

        /*
         * Fetch LoggedIn user info
         * */
        UserDTO userDTO = userService.getLoggedInUserDTO();

        // to check if logged-in user is passenger or not!
        if(userDTO.getAuthorities().contains(AuthoritiesConstants.PASSENGER)) {
            List<RideRequest> rideRequests = rideRequestRepository.findAllByUserAndDateAndNameAndRequestStatus(userMapper.userDTOToUser(userDTO), childName, requestStatus, date);
            if (!rideRequests.isEmpty()) {
                rideRequests.forEach(rideRequest -> {

                    RideRequestDTOv2 rideRequestDTOv2 = new RideRequestDTOv2();

                    //check if day is there in rideRequestFrequencies!
                    List<RideRequestFrequencyRelationshipDTO> list = rideRequestFrequencyRelationshipService.findAllByRideRequestId(rideRequest.getId());

                    // to get unique rideFrequency Ids
                    Set<Long> rideFrequencyIds = new HashSet<>();
                    if (!list.isEmpty()) {
                        list.forEach(rideRequestFrequencyRelationshipDTO -> {
                            rideFrequencyIds.add(rideRequestFrequencyRelationshipDTO.getRideFrequencyId());
                        });
                    }

                    // get rideFrequencies and store it to set
                    if (!rideFrequencyIds.isEmpty()) {
                        rideFrequencyIds.forEach(rideFrequencyId -> {
                            rideFrequencyService.findOne(rideFrequencyId).ifPresent(rideFrequencyDTO -> {
                                // check if day of date in parameter, present in rideFrequencies!
                                if (rideFrequencyDTO.getDay().name().equalsIgnoreCase(date.getDayOfWeek().toString())) {
                                    rideRequestDTOv2.setRideRequestDTO(this.toDTO(rideRequest));
                                    rideRequestDTOv2.setCurrentJourneyEvent(this.getCurrentJourneyEvent(rideRequest.getId(), date));
                                    rideRequestDTOSv2.add(rideRequestDTOv2);
                                }
                            });
                        });
                    }

                });
            }
        }
        return rideRequestDTOSv2;
    }

    /**
     * API used by Mobile app - Driver Today's Trip sorted by pickup time and drop off time
     * */
    @Override
    public List<RideRequestDTO> findAllRideRequestByUserAndDateOrderByPickupTimeThenDropoffTime(LocalDate date, Session session) {
        log.info("Request to get ordered RideRequest for User(driver) by date and session ordered by pickup and then drop-off time: {} {}", date, session);
        UserDTO userDTO = userService.getLoggedInUserDTO();
        List<RideRequestDTO> rideRequestDTOS = findAllRideRequestByUserAndDate(userDTO.getId(), date);

        List<RideRequestDTO> sessionedRideRequestDTOS = new ArrayList<>();

        // filter by Session provided

        if (session.equals(Session.MORNING)) {
            log.info("Filter ride requests by Session: {}", session);
            sessionedRideRequestDTOS = rideRequestDTOS.stream()
                .filter(rideRequestDTO -> {
                    return rideRequestDTO.getFrequencies().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("No frequency present in ride request!"))
                        .getDeadlineSchool() != null &&
                        this.convert(rideRequestDTO.getFrequencies().stream().findFirst()
                            .orElseThrow(() -> new RuntimeException("No frequency present in ride request!"))
                            .getDeadlineSchool()) <= maxDropoffTime;
                })
                .collect(Collectors.toList());
        }

        if (session.equals(Session.AFTERNOON)) {
            log.info("Filter ride requests by Session: {}", session);
            sessionedRideRequestDTOS = rideRequestDTOS.stream()
                .filter(rideRequestDTO -> {
                    return rideRequestDTO.getFrequencies().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("No frequency present in ride request!"))
                        .getClosingTime() != null &&
                        this.convert(rideRequestDTO.getFrequencies().stream().findFirst()
                            .orElseThrow(() -> new RuntimeException("No frequency present in ride request!"))
                            .getClosingTime()) >= minPickupTime;
                })
                .collect(Collectors.toList());
        }

        // sort by pickup time first & if pickup is same then sort using dropoff time

        if (sessionedRideRequestDTOS != null && sessionedRideRequestDTOS.size()>0) {

            Comparator<RideRequestDTO> compareByPickUp = Comparator.comparing( rideRequestDTO ->
                rideRequestDTO.getFrequencies().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No frequency present in ride request!"))
                    .getClosingTime(), nullsLast(naturalOrder())); // sort by pickup time & to maintain null entries for pickup (i.e, frequencies with only drop-off!)

            Comparator<RideRequestDTO> compareByDropOff = Comparator.comparing( rideRequestDTO ->
                rideRequestDTO.getFrequencies().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No frequency present in ride request!"))
                    .getDeadlineSchool(), nullsLast(naturalOrder())); // sort by dropOff time & to maintain null entries for dropOff(i.e, frequencies with only pickup!)

            sessionedRideRequestDTOS.sort(compareByPickUp.thenComparing(compareByDropOff));
        }

        return sessionedRideRequestDTOS;
    }

    private double convert(Long deadlineSchool) {
        double secondMultiple = 60;
        double minuteMultiple = 60;
        double miliSecondMultiple = 1000;
        return (double)deadlineSchool/ minuteMultiple / secondMultiple / miliSecondMultiple;
    }

    @Override
    public RideRequestDTO updateFromWeb(RideRequestDTO rideRequestDTO) {

        log.debug("Request to update RideRequest from web : {}", rideRequestDTO);

        /*
          Make sure Parent(Passenger) is real
          */
        User user = userService.getUserWithAuthorities(rideRequestDTO.getUserId())
            .orElseThrow(() -> new RuntimeException("Unknown user id " + rideRequestDTO.getUserId()));
        PassengerDTO passengerDTO = passengerService.findByUser(user)
            .orElseThrow(() -> new RuntimeException("Invalid user"));

        /*
         * Make sure Child is of Passenger & not of someone else
         * */
        ChildDTO childDTO = childService.findOne(rideRequestDTO.getChildId())
            .orElseThrow(() -> new RuntimeException("Invalid child"));

        if(!childDTO.getUserId().equals(passengerDTO.getUserId()))
            throw new RuntimeException("Invalid child and parent relationship");

        /*
         * Create/update Pickup Location & Drop Location, enter in DB
         * */
        if(rideRequestDTO.getPickupLocation() == null && rideRequestDTO.getDropLocation() == null)
            throw new RuntimeException("Pickup or Drop location is required.");

        if(rideRequestDTO.getPickupLocation() != null) {
            LocationDTO pickupLocationDTO = locationService.save(rideRequestDTO.getPickupLocation());
            if (pickupLocationDTO != null && pickupLocationDTO.getId() != null)
                rideRequestDTO.setPickupLocationId(pickupLocationDTO.getId());
        }

        if(rideRequestDTO.getDropLocation() != null) {
            LocationDTO dropLocationDTO = locationService.save(rideRequestDTO.getDropLocation());
            if (dropLocationDTO != null && dropLocationDTO.getId() != null)
                rideRequestDTO.setDropLocationId(dropLocationDTO.getId());
        }

        /*
         * save rideRequest
         * */
        final RideRequest rideRequest = rideRequestRepository.save(rideRequestMapper.toEntity(rideRequestDTO));
        if(rideRequest == null || rideRequest.getId() == null)
            throw new RuntimeException("Unable to create/update Ride request " + rideRequestDTO.toString() +", Rolling back...");

        /*
         * Create/update Frequency
         * */
        List<RideFrequencyDTO> rideFrequencyDTOS;
        if(rideRequestDTO.getFrequencies() != null && rideRequestDTO.getFrequencies().size() > 0) {
            rideFrequencyDTOS = rideRequestDTO.getFrequencies()
                .stream().map(rideFrequencyDTO -> {
                    RideFrequencyDTO dto = rideFrequencyService.save(rideFrequencyDTO);
                    if (dto != null && dto.getId() != null)
                        return dto;
                    else
                        throw new RuntimeException("Unable to create Frequency " + rideFrequencyDTO.toString() + ", Rolling back...");
                }).collect(Collectors.toList());
        } else {
            throw new RuntimeException("Frequencies is required.");
        }

        /*
         * Update FrequencyId & RideRequestId in
         * RIDE_REQ_FREQ_RELATIONSHIP TABLE
         * */
        if(rideFrequencyDTOS != null && rideFrequencyDTOS.size() > 0) {

            // delete old rideRequestFrequencyRelationship by rideRequestId
            rideRequestFrequencyRelationshipService.deleteByRideRequestId(rideRequestDTO.getId());

            // save new rideRequestFrequencyRelationship

            rideFrequencyDTOS.stream().forEach(rideFrequencyDTO -> {
                RideRequestFrequencyRelationshipDTO dto = rideRequestFrequencyRelationshipService.save(
                    new RideRequestFrequencyRelationshipDTO(rideRequest.getId(), rideFrequencyDTO.getId()));
                if (dto == null || dto.getId() == null)
                    throw new RuntimeException("Unable to create relationship of rideRequest=" + rideRequest.getId()
                        + ", rideFrequency=" + rideFrequencyDTO.getId());
            });
        }

        return rideRequestMapper.toDto(rideRequest);
    }

    private String getCurrentJourneyEvent(Long rideRequestId, LocalDate date) {
        JourneyEventDTO journeyEventDTO;
        String eventStr = "";
        if (rideRequestId != null) {
            JourneyPlanDTO journeyPlanDTO = journeyPlanService.findByDateAndRideRequestId(date, rideRequestId);
            if (journeyPlanDTO!=null) {
                List<JourneyEventDTO> journeyEventDTOS = journeyEventService.findAllByJourneyPlanIdOrderByTimestampDesc(journeyPlanDTO.getId());

                if (!(journeyEventDTOS.isEmpty())) {
                    journeyEventDTO = journeyEventDTOS.get(0); // to get latest event
                    eventStr = journeyEventDTO.getEvent().toString();
                }
            }
        }
        return eventStr;
    }

}
