package de.zweelo.gateway.service.dto;
import java.time.LocalDate;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.zweelo.gateway.domain.enumeration.RequestStatus;

/**
 * A DTO for the {@link de.zweelo.gateway.domain.RideRequest} entity.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RideRequestDTO implements Serializable {

    private Long id;

    private LocalDate startDate;

    private LocalDate endDate;

    private RequestStatus requestStatus;

    private String requestComment;

    private Long userId;

    private String userLogin;

    private Long childId;

    private LocationDTO pickupLocation;

    private LocationDTO dropLocation;

    private Long pickupLocationId;

    private Long dropLocationId;

    private Set<RideFrequencyDTO> frequencies;

    @JsonProperty("journey")
    private JourneyPlanDTO journeyPlanDTO;

    @JsonProperty("user")
    private PassengerDTO passengerDTO;

    @JsonProperty("child")
    private ChildDTO childDTO;

    @JsonProperty("request")
    private RideManagementDTO rideManagementDTO;

    @JsonProperty("driver")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private DriverDTO driverDTO;

    public DriverDTO getDriverDTO() {
        return driverDTO;
    }

    public void setDriverDTO(DriverDTO driverDTO) {
        this.driverDTO = driverDTO;
    }

    public RideRequestDTO() {
    }

    public JourneyPlanDTO getJourneyPlanDTO() {
        return journeyPlanDTO;
    }

    public void setJourneyPlanDTO(JourneyPlanDTO journeyPlanDTO) {
        this.journeyPlanDTO = journeyPlanDTO;
    }

    public RideManagementDTO getRideManagementDTO() {
        return rideManagementDTO;
    }

    public void setRideManagementDTO(RideManagementDTO rideManagementDTO) {
        this.rideManagementDTO = rideManagementDTO;
    }

    public PassengerDTO getPassengerDTO() {
        return passengerDTO;
    }

    public void setPassengerDTO(PassengerDTO passengerDTO) {
        this.passengerDTO = passengerDTO;
    }

    public ChildDTO getChildDTO() {
        return childDTO;
    }

    public void setChildDTO(ChildDTO childDTO) {
        this.childDTO = childDTO;
    }

    public Set<RideFrequencyDTO> getFrequencies() {
        return frequencies;
    }

    public void setFrequencies(Set<RideFrequencyDTO> frequencies) {
        this.frequencies = frequencies;
    }

    public Long getPickupLocationId() {
        return pickupLocationId;
    }

    public void setPickupLocationId(Long pickupLocationId) {
        this.pickupLocationId = pickupLocationId;
    }

    public Long getDropLocationId() {
        return dropLocationId;
    }

    public void setDropLocationId(Long dropLocationId) {
        this.dropLocationId = dropLocationId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getRequestComment() {
        return requestComment;
    }

    public void setRequestComment(String requestComment) {
        this.requestComment = requestComment;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public Long getChildId() {
        return childId;
    }

    public void setChildId(Long childId) {
        this.childId = childId;
    }

    public LocationDTO getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(LocationDTO pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public LocationDTO getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(LocationDTO dropLocation) {
        this.dropLocation = dropLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RideRequestDTO rideRequestDTO = (RideRequestDTO) o;
        if (rideRequestDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), rideRequestDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "RideRequestDTO{" +
            "id=" + getId() +
            ", startDate='" + getStartDate() + "'" +
            ", endDate='" + getEndDate() + "'" +
            ", requestStatus='" + getRequestStatus() + "'" +
            ", requestComment='" + getRequestComment() + "'" +
            ", userId=" + getUserId() +
            ", userLogin='" + getUserLogin() + "'" +
            ", childId=" + getChildId() +
            ", pickupLocation=" + getPickupLocation() +
            ", dropLocation=" + getDropLocation() +
            "}";
    }
}
