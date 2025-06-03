package com.mingle.entity;


import com.google.protobuf.ByteString;
import com.mingle.MingleUserDto;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import io.quarkus.security.jpa.Password;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.mingle.utility.Formatters.toLocalDate;

@EqualsAndHashCode(callSuper = true)
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
@FieldNameConstants
@Data
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"email"}),
                @UniqueConstraint(columnNames = {"user_name"}),
        },
        name = "mingle_user"
)
public class MingleUser extends PanacheEntity {

    @Embedded
    Audit audit;

    private String bio;

    @Lob
    private byte[] image;

    @Column(name="first_name",nullable = false)
    private String firstname;

    @Column(name="last_name",nullable = false)
    private String lastname;

    @Username
    @Column(name="user_name",unique = true, nullable = false)
    private String username;


    @Column(nullable = false)
    private String zip;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "is_active")
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    private Relationship relationship;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Skill skill;

    @Column(nullable = false)
    private LocalDate birthday;

    @Enumerated(EnumType.STRING)
    @Column(name="sport_type")
    private SportType sportType;


    @Builder.Default
    @OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL, mappedBy = "organizer")
    private List<MingleGroup> mingleGroup = List.of() ;


    public MingleUser(MingleUserDto mingleUserDto){
        if(mingleUserDto.getId()!=0L)
            this.id=mingleUserDto.getId();
        this.bio=mingleUserDto.getBio();
        this.image= mingleUserDto.getImage().toByteArray();
        this.firstname=mingleUserDto.getFirstname();
        this.lastname=mingleUserDto.getLastname();
        this.username=mingleUserDto.getUsername();
        this.zip=mingleUserDto.getZip();
        this.email=mingleUserDto.getEmail();
        this.phone=mingleUserDto.getPhone();
        this.birthday= toLocalDate(mingleUserDto.getBirthday());
        this.relationship= Relationship.valueOf(mingleUserDto.getRelationship());
        this.gender= Gender.valueOf(mingleUserDto.getGender());
        this.skill = Skill.valueOf(mingleUserDto.getSkill());
        this.sportType=SportType.valueOf(mingleUserDto.getSportType());
        this.setMingleGroup(
                mingleUserDto.getMingleGroupDtoList().stream().map(MingleGroup::new).toList()
        );
        this.getMingleGroup().forEach(t->t.setOrganizer(this));
    }

    public MingleUserDto toMingleUserDto(){
        MingleUserDto.Builder builder =MingleUserDto.newBuilder()
                .setId(this.id)
                .setBio(this.bio)
                .setFirstname(this.firstname)
                .setLastname(this.lastname)
                .setUsername(this.username)
                .setZip(this.zip)
                .setEmail(this.email)
                .setPhone(this.phone)
                .setBirthday(String.valueOf(this.birthday))
                .setSportType(String.valueOf(this.sportType))
                .setRelationship(String.valueOf(this.relationship))
                .setGender(String.valueOf(this.gender))
                .setSkill(String.valueOf(this.skill))
                .addAllMingleGroupDto(
                        this.getMingleGroup().stream().map(MingleGroup::toMingleGroupDto
                        ).toList()
                );
        Optional.ofNullable(this.image).ifPresent(t->builder.setImage(ByteString.copyFrom(t)));
        return builder.build();
    }

    public MingleUser updateMingleUser(MingleUserDto mingleUserDto){
                this.id=mingleUserDto.getId();
                this.bio=mingleUserDto.getBio();
                this.firstname=mingleUserDto.getFirstname();
                this.lastname=mingleUserDto.getLastname();
                this.username=mingleUserDto.getUsername();
                this.zip=mingleUserDto.getZip();
                this.email=mingleUserDto.getEmail();
                this.phone=mingleUserDto.getPhone();
                this.birthday= toLocalDate(mingleUserDto.getBirthday());
                this.image=mingleUserDto.getImage().toByteArray();
                this.relationship= Relationship.valueOf(mingleUserDto.getRelationship());
                this.gender= Gender.valueOf(mingleUserDto.getGender());
                this.skill= Skill.valueOf(mingleUserDto.getSkill());
                this.sportType=SportType.valueOf(mingleUserDto.getSportType());
                this.audit.setUpdatedDttm(LocalDateTime.now());
                this.audit.setUpdatedBy(this.username);
                return this;
    }
    public enum Relationship {
        S,
        R
    }

    public enum Gender {
        M,
        F
    }

    public enum Skill {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED
    }
    public enum SportType{
        COED,EXCLUSIVE
    }


}