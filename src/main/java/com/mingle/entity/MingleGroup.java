package com.mingle.entity;

import com.google.protobuf.ByteString;
import com.mingle.MingleGroupDto;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;

@EqualsAndHashCode(callSuper = true)
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@Data
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"zip", "groupName"})
)
public class MingleGroup extends PanacheEntity {

    private String zip;

    private String groupName;

    @Embedded
    private Audit audit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false,updatable = false,foreignKey = @ForeignKey(name="FK_MingleUser_organizer"))
    private MingleUser organizer;

    private String description;

    @Lob
    private byte[] images;

    private Boolean isActive;

    public MingleGroupDto toMingleGroupDto(){

        return MingleGroupDto.newBuilder()
                .setDescription(description)
                .setGroupName(groupName)
                .setOrganizerId(organizer.id)
                .setZip(zip)
                .setImages(ByteString.copyFrom(images))
                .build();
    }

    public MingleGroup(MingleGroupDto mingleGroupDto){
        this.groupName=mingleGroupDto.getGroupName();
        this.setOrganizer( new MingleUser(mingleGroupDto.getId()));
        this.description=mingleGroupDto.getDescription();
        this.images= mingleGroupDto.getImages().toByteArray();
        this.zip=mingleGroupDto.getZip();
    }

}
