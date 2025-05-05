package com.mingle.entity;


import com.mingle.MingleLocationDto;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "mingle_location",
        uniqueConstraints = @UniqueConstraint(columnNames = {"location_name", "mingle_league_id"})
)
public class MingleLocation extends PanacheEntity {

    @Embedded
    private Audit audit;

    @Column(name="host_email",nullable = false)
    private String hostEmail;

    @Column(name="host_name",nullable = false)
    private String hostName;

    @Column(name="location_address",nullable = false)
    private String locationAddress;

    @Column(name="zip_code",nullable = false)
    private String zipCode;

    @Column(name="location_name",nullable = false)
    private String locationName;

    @Column
    private String description; // Optional description field

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mingle_league_id", nullable = false,updatable = false, foreignKey = @ForeignKey(name="location_league"))
    private MingleLeague mingleLeague ;

    @OneToMany(fetch = FetchType.EAGER,mappedBy ="mingleLocation", cascade = CascadeType.ALL)
    @Builder.Default
    private List<MingleTimeSlot> mingleTimeSlot = new ArrayList<>();



    public MingleLocation(MingleLocationDto mingleLocation) {
        this.hostEmail=mingleLocation.getHostEmail();
        this.hostName= mingleLocation.getHostName();
        this.locationAddress= mingleLocation.getLocationAddress();
        this.zipCode=mingleLocation.getZipCode();
        this.locationName= mingleLocation.getLocationName();
        this.description= mingleLocation.getDescription();// Optional description field
        this.mingleTimeSlot=mingleLocation.getMingleTimeSlotDtoList().stream().map(MingleTimeSlot::new).toList();
        this.getMingleTimeSlot().forEach(t->t.setMingleLocation(this));
    }
    public MingleLocationDto getMingleLocationDto(){
        return MingleLocationDto.newBuilder()
                .setHostEmail(this.hostEmail)
                .setHostName(this.hostName)
                .setLocationAddress(this.locationAddress)
                .setZipCode(this.zipCode)
                .setLocationName(this.locationName)
                .setDescription(this.description)
                .addAllMingleTimeSlotDto(this.mingleTimeSlot.stream().map(MingleTimeSlot::toMingleTimeSlotDto).toList())
                .build();

    }


}

