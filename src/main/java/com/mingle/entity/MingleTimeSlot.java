package com.mingle.entity;

import com.mingle.MingleTimeSlotDto;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "mingle_time_slot",
        uniqueConstraints = @UniqueConstraint(columnNames = {
                "mingle_location_id", "start_time","end_time","day_of_week"
        })
)
public class MingleTimeSlot extends PanacheEntity {

    @Embedded
    private Audit audit;

    @Column(nullable = false,name = "start_time")
    private String startTime;

    @Column(nullable = false,name = "end_time")
    private String endTime;

    @Column(nullable = false,name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @Builder.Default
    @Column(name = "is_playable")
    Boolean isPlayable=true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    Reoccurrence reoccurrence = Reoccurrence.WEEKLY;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="mingle_location_id",nullable = false,updatable = false,foreignKey = @ForeignKey(name="time_slot_location_league"))
    private MingleLocation mingleLocation;



    enum Reoccurrence{
        BIWEEKLY,WEEKLY,ONCE,MONTHLY,DAILY
    }

    public MingleTimeSlot(MingleTimeSlotDto mingleTimeSlotDto) {
        startTime=mingleTimeSlotDto.getStartTime();
        endTime=mingleTimeSlotDto.getEndTime();
        dayOfWeek=DayOfWeek.valueOf(mingleTimeSlotDto.getDayOfWeek());
        isPlayable=mingleTimeSlotDto.getIsPlayable();
        reoccurrence=Reoccurrence.valueOf(mingleTimeSlotDto.getReoccurrence());
        mingleLocation=new MingleLocation(mingleTimeSlotDto.getMingleLocationDto());
    }
    public MingleTimeSlotDto toMingleTimeSlotDto(){
        return MingleTimeSlotDto.newBuilder()
                .setStartTime(this.startTime)
                .setEndTime(this.endTime)
                .setDayOfWeek(this.dayOfWeek.name())
                .setIsPlayable(this.isPlayable)
                .setReoccurrence(this.reoccurrence.name())
                .build();
    }
}
