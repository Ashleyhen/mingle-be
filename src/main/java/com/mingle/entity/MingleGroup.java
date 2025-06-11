package com.mingle.entity;

import com.google.protobuf.ByteString;
import com.mingle.MingleGroupDto;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@Data
@Table(
        name = "mingle_group",
        uniqueConstraints = @UniqueConstraint(columnNames = {"zip", "group_name"})
)
public class MingleGroup extends PanacheEntity {

    @Column(nullable = false)
    private String zip;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Embedded
    private Audit audit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false,updatable = false,foreignKey = @ForeignKey(name="FK_MingleUser_organizer"))
    private MingleUser organizer;


    @Builder.Default
    @OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL,mappedBy = "mingleGroup")
    private List<MingleLeague> league = new ArrayList<>();

    private String description;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    @Lob
    @Builder.Default
    private byte[] images=new byte[]{};

    @Column(name = "is_active")
    private Boolean isActive;

    public MingleGroupDto toMingleGroupDto(){
        return MingleGroupDto.newBuilder()
                .setId(this.id)
                .setDescription(description)
                .setGroupName(groupName)
                .setZip(zip)
                .setImages(images == null?ByteString.empty():ByteString.copyFrom(images))
                .addAllMingleLeagueDto(league.stream().map(MingleLeague::getMingleLeagueDto).toList())
                .build();
    }

    public MingleGroup(MingleGroupDto mingleGroupDto){
        this.groupName=mingleGroupDto.getGroupName();
        this.description=mingleGroupDto.getDescription();
        this.images= mingleGroupDto.getImages().toByteArray();
        this.zip=mingleGroupDto.getZip();
        this.league=mingleGroupDto.getMingleLeagueDtoList().stream().map(MingleLeague::new).toList();
        this.getLeague().forEach(t->t.setMingleGroup(this));
    }

}
