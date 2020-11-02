package de.zweelo.gateway.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

import de.zweelo.gateway.domain.enumeration.RequestStatus;

/**
 * A RideRequest.
 */
@Entity
@Table(name = "ride_request")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class RideRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private RequestStatus requestStatus;

    @Column(name = "request_comment")
    private String requestComment;

    @ManyToOne
    @JsonIgnoreProperties("rideRequests")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties("rideRequests")
    private Child child;

    @ManyToOne
    @JoinColumn(nullable = true)
    @JsonIgnoreProperties("rideRequests")
    private Location pickupLocation;

    @ManyToOne
    @JoinColumn(nullable = true)
    @JsonIgnoreProperties("rideRequests")
    private Location dropLocation;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public RideRequest startDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public RideRequest endDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public RideRequest requestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
        return this;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getRequestComment() {
        return requestComment;
    }

    public RideRequest requestComment(String requestComment) {
        this.requestComment = requestComment;
        return this;
    }

    public void setRequestComment(String requestComment) {
        this.requestComment = requestComment;
    }

    public User getUser() {
        return user;
    }

    public RideRequest user(User user) {
        this.user = user;
        return this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Child getChild() {
        return child;
    }

    public RideRequest child(Child child) {
        this.child = child;
        return this;
    }

    public void setChild(Child child) {
        this.child = child;
    }

    public Location getPickupLocation() {
        return pickupLocation;
    }

    public RideRequest pickupLocation(Location location) {
        this.pickupLocation = location;
        return this;
    }

    public void setPickupLocation(Location location) {
        this.pickupLocation = location;
    }

    public Location getDropLocation() {
        return dropLocation;
    }

    public RideRequest dropLocation(Location location) {
        this.dropLocation = location;
        return this;
    }

    public void setDropLocation(Location location) {
        this.dropLocation = location;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RideRequest)) {
            return false;
        }
        return id != null && id.equals(((RideRequest) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "RideRequest{" +
            "id=" + getId() +
            ", startDate='" + getStartDate() + "'" +
            ", endDate='" + getEndDate() + "'" +
            ", requestStatus='" + getRequestStatus() + "'" +
            ", requestComment='" + getRequestComment() + "'" +
            "}";
    }
}
