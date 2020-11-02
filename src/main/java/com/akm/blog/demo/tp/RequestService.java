package de.zweelo.gateway.service;

import de.zweelo.gateway.domain.enumeration.RequestStatus;
import de.zweelo.gateway.domain.enumeration.Session;
import de.zweelo.gateway.service.dto.DriverJourneyDTO;
import de.zweelo.gateway.service.dto.RideRequestDTO;

import de.zweelo.gateway.service.dto.RideRequestDTOv2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link de.zweelo.gateway.domain.RideRequest}.
 */
public interface RideRequestService {

    /**
     * Save a rideRequest.
     *
     * @param rideRequestDTO the entity to save.
     * @return the persisted entity.
     */
    RideRequestDTO save(RideRequestDTO rideRequestDTO);

    /**
     * Save a rideRequest from web.
     *
     * @param rideRequestDTO the entity to save.
     * @return the persisted entity.
     */
    RideRequestDTO saveFromWeb(RideRequestDTO rideRequestDTO);


    /**
     * Get all the rideRequests.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<RideRequestDTO> findAll(Pageable pageable);

    /**
     * Get all the rideRequests by RequestStatus
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<RideRequestDTO> findByStatus(RequestStatus requestStatus, Pageable pageable);

    /**
     * Get all the rideRequests by date.
     *
     * @param date the journey date
     * @return the list of entities.
     */
    List<RideRequestDTO> findAllJourneys(LocalDate date);

    /**
     * Get all the rideRequests by UserID(FROM TOKEN) & date.
     *
     * @param date the journey date
     * @return the list of entities.
     */
    List<RideRequestDTO> findAllRideRequestByUserAndDate(LocalDate date);


    /**
     * Get all the rideRequests by UserID & date.
     *
     * @param date the journey date
     * @param userId User Id of Driver
     * @return the list of entities.
     */
    List<RideRequestDTO> findAllRideRequestByUserAndDate(long userId, LocalDate date);

    /**
     * Get all the rideRequests by UserID(FROM TOKEN).
     *
     * @return the list of entities.
     */
    public DriverJourneyDTO findAllJourneysByDriver();


    /**
     * Get the "id" rideRequest.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<RideRequestDTO> findOne(Long id);

    /**
     * Get the "Passenger's UserID" long.
     *
     * @param journeyPlanId the id of the entity.
     * @return the long.
     */
    public long findPassengerUserIdByJourneyPlanId(Long journeyPlanId);

    /**
     * Delete the "id" rideRequest.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    /**
     * Delete the rideRequest.
     *
     * @param userId the id of the entity.
     */
    void deleteByUserId(Long userId);

    /**
     * Get all the rideRequests by UserID(FROM TOKEN) & status.
     *
     * @param requestStatus the status of ride request
     * @return the list of entities.
     */
    List<RideRequestDTO> findAllRideRequestByUserAndRequestStatus(RequestStatus requestStatus);

    /**
     * Get all the rideRequests by UserID(FROM TOKEN) & status.
     *
     * @param date the date in ride request
     * @return the list of entities.
     */
    List<RideRequestDTO> findAllByUserAndDate(LocalDate date);

    /**
     * Get all the rideRequests by UserID(FROM TOKEN), status & date.
     *
     * @param date the date in ride request
     * @param requestStatus the status of ride request
     * @return the list of entities.
     */
    List<RideRequestDTO> findAllRideRequestByUserAndDateAndRequestStatus(RequestStatus requestStatus, LocalDate date);

    /**
     * Get all the rideRequests by UserID(FROM TOKEN), status, childName & date.
     *
     * @param date the date in ride request
     * @param childName the childName in ride request
     * @param requestStatus the status of ride request
     * @return the list of entities.
     */
    List<RideRequestDTO> findAllRideRequestByUserAndDateAndNameAndRequestStatus(RequestStatus requestStatus, String childName, LocalDate date);

    /**
     * Get all the rideRequests by UserID(FROM TOKEN), status, childName & date.
     *
     * @param date the date in ride request
     * @param childName the childName in ride request
     * @param requestStatus the status of ride request
     * @return the list of entities with journeyEvent.
     */
    List<RideRequestDTOv2> findAllRideRequestByUserAndDateAndNameAndRequestStatusV2(RequestStatus requestStatus, String childName, LocalDate date);

    /**
     * Get all the rideRequests by UserID(FROM TOKEN) & date, session and ordered by pickup time then drop off time.
     *
     * @param date the journey date
     * @param session session of the day - MORNING/AFTERNOON
     * @return the list of entities.
     */
    List<RideRequestDTO> findAllRideRequestByUserAndDateOrderByPickupTimeThenDropoffTime(LocalDate date, Session session);

    /**
     * Update a rideRequest from web.
     *
     * @param rideRequestDTO the entity to save.
     * @return the persisted entity.
     */
    RideRequestDTO updateFromWeb(RideRequestDTO rideRequestDTO);
}
