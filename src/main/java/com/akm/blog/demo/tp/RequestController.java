package de.zweelo.gateway.web.rest;

import de.zweelo.gateway.domain.enumeration.RequestStatus;
import de.zweelo.gateway.domain.enumeration.Session;
import de.zweelo.gateway.service.RideRequestService;
import de.zweelo.gateway.service.dto.DriverJourneyDTO;
import de.zweelo.gateway.service.dto.RideRequestDTOv2;
import de.zweelo.gateway.web.rest.errors.BadRequestAlertException;
import de.zweelo.gateway.service.dto.RideRequestDTO;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.zweelo.gateway.domain.RideRequest}.
 */
@RestController
@RequestMapping("/api")
public class RideRequestResource {

    private final Logger log = LoggerFactory.getLogger(RideRequestResource.class);

    private static final String ENTITY_NAME = "rideRequest";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RideRequestService rideRequestService;

    public RideRequestResource(RideRequestService rideRequestService) {
        this.rideRequestService = rideRequestService;
    }

    /**
     * {@code POST  /ride-requests} : Create a new rideRequest.
     *
     * @param rideRequestDTO the rideRequestDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new rideRequestDTO, or with status {@code 400 (Bad Request)} if the rideRequest has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/ride-requests")
    public ResponseEntity<RideRequestDTO> createRideRequest(@RequestBody RideRequestDTO rideRequestDTO) throws URISyntaxException {
        log.info("REST request to save RideRequest : {}", rideRequestDTO);
        if (rideRequestDTO.getId() != null) {
            throw new BadRequestAlertException("A new rideRequest cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RideRequestDTO result = rideRequestService.save(rideRequestDTO);
        return ResponseEntity.created(new URI("/api/ride-requests/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code POST  /ride-requests} : Create a new rideRequest.
     * Only admin can create rideRequest
     * @param rideRequestDTO the rideRequestDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new rideRequestDTO, or with status {@code 400 (Bad Request)} if the rideRequest has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/ride-requests/web")
    public ResponseEntity<RideRequestDTO> createRideRequestByAdmin(@RequestBody RideRequestDTO rideRequestDTO) throws URISyntaxException {
        log.info("REST request to save RideRequest by Admin : {}", rideRequestDTO);
        if (rideRequestDTO.getId() != null) {
            throw new BadRequestAlertException("A new rideRequest cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RideRequestDTO result = rideRequestService.saveFromWeb(rideRequestDTO);
        return ResponseEntity.created(new URI("/api/ride-requests/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /ride-requests} : Updates an existing rideRequest.
     *
     * @param rideRequestDTO the rideRequestDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated rideRequestDTO,
     * or with status {@code 400 (Bad Request)} if the rideRequestDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the rideRequestDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/ride-requests")
    public ResponseEntity<RideRequestDTO> updateRideRequest(@RequestBody RideRequestDTO rideRequestDTO) throws URISyntaxException {
        log.info("REST request to update RideRequest : {}", rideRequestDTO);
        if (rideRequestDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        RideRequestDTO result = rideRequestService.save(rideRequestDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, rideRequestDTO.getId().toString()))
            .body(result);
    }


    /**
     * {@code PUT  /ride-requests} : Updates an existing rideRequest.
     * Only admin can edit/ update rideRequest from admin panel
     * @param rideRequestDTO the rideRequestDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated rideRequestDTO,
     * or with status {@code 400 (Bad Request)} if the rideRequestDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the rideRequestDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/ride-requests/web")
    public ResponseEntity<RideRequestDTO> updateRideRequestFromWeb(@RequestBody RideRequestDTO rideRequestDTO) throws URISyntaxException {
        log.info("REST request to update RideRequest from web : {}", rideRequestDTO);
        if (rideRequestDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        RideRequestDTO result = rideRequestService.updateFromWeb(rideRequestDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, rideRequestDTO.getId().toString()))
            .body(result);
    }


    /**
     * {@code PUT  /ride-requests} : Updates an existing rideRequest.
     *
     * @param rideRequestDTO the rideRequestDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated rideRequestDTO,
     * or with status {@code 400 (Bad Request)} if the rideRequestDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the rideRequestDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/ride-requests/status")
    public ResponseEntity<RideRequestDTO> updateRideRequestStatus(@RequestBody RideRequestDTO rideRequestDTO) throws URISyntaxException {
        log.info("REST request to update RideRequest Status : {}", rideRequestDTO);
        if (rideRequestDTO.getId() == null || rideRequestDTO.getRequestStatus() == null) {
            throw new BadRequestAlertException("Invalid id or status", ENTITY_NAME, "id or status null");
        }

        RideRequestDTO result = rideRequestService.save(rideRequestDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, rideRequestDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /ride-requests} : get all the rideRequests.
     *

     * @param pageable the pagination information.

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of rideRequests in body.
     */
    @GetMapping("/ride-requests")
    public ResponseEntity<List<RideRequestDTO>> getAllRideRequests(Pageable pageable) {
        log.info("REST request to get a page of RideRequests");
        Page<RideRequestDTO> page = rideRequestService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /ride-requests/:id} : get the "id" rideRequest.
     *
     * @param id the id of the rideRequestDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rideRequestDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ride-requests/{id}")
    public ResponseEntity<RideRequestDTO> getRideRequest(@PathVariable Long id) {
        log.info("REST request to get RideRequest : {}", id);
        Optional<RideRequestDTO> rideRequestDTO = rideRequestService.findOne(id);
        return ResponseUtil.wrapOrNotFound(rideRequestDTO);
    }

    /**
     * {@code GET  /ride-requests/status/:requestStatus} : get requestStatus by Ride Request Status
     *
     * @param requestStatus the Ride RequestStatus of the rideRequestDTO to retrieve.
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rideRequestDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ride-requests/status/{status}")
    public ResponseEntity<List<RideRequestDTO>> getRideRequestByStatus(@PathVariable("status") String requestStatus, Pageable pageable) {
        log.info("REST request to get RideRequest : {}", requestStatus);

        Page<RideRequestDTO> page = rideRequestService.findByStatus(Enum.valueOf(RequestStatus.class, requestStatus), pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /ride-requests/date/{date}} : get All RideRequest by Date
     *
     * @param date of Journeys
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rideRequestDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ride-requests/date/{date}")
    public ResponseEntity<List<RideRequestDTO>> getRideRequestForTomorrow(@PathVariable LocalDate date) {
        log.info("REST request to get RideRequest for : {}", date);
        List<RideRequestDTO> journeys = rideRequestService.findAllJourneys(date);
        return ResponseEntity.ok().body(journeys);
    }

    /**
     * {@code GET  /ride-requests/journey/{date}} : get All RideRequest by Date for Driver - Based on token
     *
     * @param date
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rideRequestDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ride-requests/journey/{date}")
    public ResponseEntity<List<RideRequestDTO>> getRideRequestByJourneyPlan(@PathVariable LocalDate date) {
        log.info("REST request to get RideRequest for User by date : {}", date);
        List<RideRequestDTO> journeys = rideRequestService.findAllRideRequestByUserAndDate(date);
        return ResponseEntity.ok().body(journeys);
    }

    /**
     *
     * API consumed by mobile app, Driver's All Journeys by Session & sorted by pickup & then by drop-off time
     *
     * {@code GET  /ride-requests/journey-v2/{date}/{session}} : get All RideRequest by Date by Session for Driver - Based on token ordered by pickup time and then drop off time
     *
     * @param date the journey date
     * @param session session of the day - MORNING/AFTERNOON
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rideRequestDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/v2/ride-requests/journey/{date}/{session}")
    public ResponseEntity<List<RideRequestDTO>> getRideRequestByJourneyPlanOrderByPickupTimeThenDropoffTime(@PathVariable("date") LocalDate date,
                                                                                                            @PathVariable("session") Session session) {
        log.info("REST request to get ordered RideRequest for User(driver) by date and session ordered by pickup and then drop-off time: {} {}", date, session);
        List<RideRequestDTO> journeys = rideRequestService.findAllRideRequestByUserAndDateOrderByPickupTimeThenDropoffTime(date, session);
        return ResponseEntity.ok().body(journeys);
    }

    /**
     * {@code GET  /ride-requests/journey/{date}} : get All RideRequest by Date for Driver - Based on token
     *
     * @param date
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rideRequestDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ride-requests/journeysByUserId")
    public ResponseEntity<List<RideRequestDTO>> getRideRequestByJourneyPlan(@RequestParam("userID") long userId, @RequestParam("date") LocalDate date) {
        log.info("REST request to get RideRequest for User {} by date : {}",userId, date);
        List<RideRequestDTO> journeys = rideRequestService.findAllRideRequestByUserAndDate(userId, date);
        if(journeys != null)
            return ResponseEntity.ok().body(journeys);
        else
            return ResponseEntity.noContent().build();
    }

    /**
     * API consumed by mobile app, Driver's All Journeys
     * */
    @GetMapping("/ride-requests/drivers-journeys/")
    public ResponseEntity<DriverJourneyDTO> getAllJourneysByDriver() {
        log.info("REST request to get getAllJourneysByDriver");
        DriverJourneyDTO journeys = rideRequestService.findAllJourneysByDriver();
        return ResponseEntity.ok().body(journeys);
    }

    /**
     * API consumed by mobile app, Passenger's All ride requests for the date
     *
     * {@code GET  /ride-requests/user/date/{date}} : get All RideRequest by passenger by Date
     *
     * @param date of Journeys
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rideRequestDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ride-requests/user/date/{date}")
    public ResponseEntity<List<RideRequestDTO>> getRideRequestByUserAndDate(@PathVariable LocalDate date) {
        log.info("REST request to get RideRequest for : {}", date);
        List<RideRequestDTO> journeys = rideRequestService.findAllByUserAndDate(date);
        return ResponseEntity.ok().body(journeys);
    }

    /**
     * API consumed by mobile app, Passenger's All ride requests by status
     *
     * {@code GET  /ride-requests/user/status/:requestStatus} : get All RideRequest by rideRequest status
     *
     * @param requestStatus the Ride RequestStatus of the rideRequestDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rideRequestDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ride-requests/user/status/{status}")
    public ResponseEntity<List<RideRequestDTO>> getRideRequestByUserAndStatus(@PathVariable("status") String requestStatus) {
        log.info("REST request to get RideRequest with requestStatus : {}", requestStatus);
        List<RideRequestDTO> journeys = rideRequestService.findAllRideRequestByUserAndRequestStatus(Enum.valueOf(RequestStatus.class, requestStatus));
        return ResponseEntity.ok().body(journeys);
    }

    /**
     * API consumed by mobile app, Passenger's All ride requests by date & status
     *
     * {@code GET  /ride-requests/user/:date/:requestStatus} : get All RideRequest by date & rideRequest status
     *
     * @param requestStatus the Ride Requests to retrieve with requestStatus
     * @param date Ride Requests to retrieve for that particular date
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rideRequestDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ride-requests/user/{date}/{status}")
    public ResponseEntity<List<RideRequestDTO>> getRideRequestByUserAndDateAndStatus(@PathVariable("date") LocalDate date, @PathVariable("status") String requestStatus) {
        log.info("REST request to get RideRequest with requestStatus : {} and date : {}", requestStatus, date);
        List<RideRequestDTO> journeys = rideRequestService.findAllRideRequestByUserAndDateAndRequestStatus(Enum.valueOf(RequestStatus.class, requestStatus), date);
        return ResponseEntity.ok().body(journeys);
    }

    /**
     * API consumed by mobile app, Passenger's All ride requests by date, childName & status
     *
     * {@code GET  /ride-requests/user/:date/:requestStatus} : get All RideRequest by date & rideRequest status
     *
     * @param requestStatus the Ride Requests to retrieve with requestStatus
     * @param childName Ride Requests to retrieve for that particular child name
     * @param date Ride Requests to retrieve for that particular date
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rideRequestDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ride-requests/user/{date}/{childName}/{status}")
    public ResponseEntity<List<RideRequestDTO>> getRideRequestByUserAndDateAndChildNameAndStatus(@PathVariable("date") LocalDate date, @PathVariable("childName") String childName, @PathVariable("status") String requestStatus) {
        log.info("REST request to get RideRequest with requestStatus : {} and childName : {} and date : {}", requestStatus, childName, date);
        List<RideRequestDTO> journeys = rideRequestService.findAllRideRequestByUserAndDateAndNameAndRequestStatus(Enum.valueOf(RequestStatus.class, requestStatus), childName, date);
        return ResponseEntity.ok().body(journeys);
    }

    /**
     * API consumed by mobile app, Passenger's All ride requests by date, childName & status
     *
     * {@code GET  /ride-requests/user/:date/:requestStatus} : get All RideRequest by date & rideRequest status
     *
     * @param requestStatus the Ride Requests to retrieve with requestStatus
     * @param childName Ride Requests to retrieve for that particular child name
     * @param date Ride Requests to retrieve for that particular date
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rideRequestDTO with journeyEvent added in response., or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ride-requests-v2/user/{date}/{childName}/{status}")
    public ResponseEntity<List<RideRequestDTOv2>> getRideRequestByUserAndDateAndChildNameAndStatusV2(@PathVariable("date") LocalDate date, @PathVariable("childName") String childName, @PathVariable("status") String requestStatus) {
        log.info("REST request to get RideRequest with requestStatus : {} and childName : {} and date : {}", requestStatus, childName, date);
        List<RideRequestDTOv2> journeys = rideRequestService.findAllRideRequestByUserAndDateAndNameAndRequestStatusV2(Enum.valueOf(RequestStatus.class, requestStatus), childName, date);
        return ResponseEntity.ok().body(journeys);
    }

    /**
     * {@code DELETE  /ride-requests/:id} : delete the "id" rideRequest.
     *
     * @param id the id of the rideRequestDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/ride-requests/{id}")
    public ResponseEntity<Void> deleteRideRequest(@PathVariable Long id) {
        log.info("REST request to delete RideRequest : {}", id);
        rideRequestService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
