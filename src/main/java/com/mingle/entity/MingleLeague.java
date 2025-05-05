package com.mingle.entity;

import com.mingle.MingleLeagueDto;
import com.mingle.utility.Formatters;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.mingle.utility.Formatters.localDateToString;
import static com.mingle.utility.Formatters.toLocalDate;

@EqualsAndHashCode(callSuper = true)
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@Data
@Table(
        name = "mingle_league",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_name", "mingle_group_id"})
)
public class MingleLeague extends PanacheEntity {

        @Embedded
        private Audit audit;

        @Column(name = "event_name", nullable = false)
        private String eventName;

        @Column(name="start_date",nullable = false)
        private LocalDate startDate;

        @Column(name="end_date",nullable = false)
        private LocalDate endDate;

        @Column(name="price_per_player",nullable = false)
        private Float pricePerPlayer;

        @Column
        private String description;

        @Column(nullable = false)
        private String duration;

        @Column(name = "player_per_team", nullable = false)
        private Integer playersPerTeam;

        @Column(name = "registration_end_date", nullable = false)
        private LocalDate registrationEndDate;

        @Builder.Default
        @Column(name="is_active")
        private Boolean isActive=false;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "mingle_group_id", nullable = false,updatable = false, foreignKey = @ForeignKey(name="League_mingle_group"))
        private MingleGroup mingleGroup;

        @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "mingleLeague")
        @Builder.Default
        private List<MingleLocation> mingleLocations = new ArrayList<>(); // Fixes mappedBy reference


        // Getters and Setters
        public MingleLeague(MingleLeagueDto mingleLeagueDto){
                this.eventName=mingleLeagueDto.getEventName();
                this.startDate= toLocalDate(mingleLeagueDto.getStartDate());
                this.endDate= toLocalDate(mingleLeagueDto.getEndDate());
                this.registrationEndDate= toLocalDate(mingleLeagueDto.getRegistrationEndDate());
                this.pricePerPlayer=mingleLeagueDto.getPricePerPlayer();
                this.description=mingleLeagueDto.getDescription();
                this.duration=mingleLeagueDto.getDuration();
                this.playersPerTeam=mingleLeagueDto.getPlayersPerTeam();
                this.mingleLocations=mingleLeagueDto
                        .getMingleLocationDtoList().stream().map(MingleLocation::new).toList();
                this.mingleLocations.forEach(t->t.setMingleLeague(this));
        }

        public MingleLeagueDto getMingleLeagueDto() {
                return MingleLeagueDto.newBuilder()
                        .setId(this.id)
                        .setEventName(this.eventName)
                        .setStartDate(localDateToString(this.startDate))
                        .setEndDate(localDateToString(this.endDate))
                        .setRegistrationEndDate(localDateToString(this.registrationEndDate))
                        .setPricePerPlayer(this.pricePerPlayer)
                        .setDescription(this.description)
                        .setDuration(this.duration)
                        .setPlayersPerTeam(this.playersPerTeam)
                        .addAllMingleLocationDto(this.mingleLocations.stream().map(MingleLocation::getMingleLocationDto).toList())
                        .build();
        }
}

