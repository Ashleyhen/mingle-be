package com.mingle.entity;


import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.protobuf.ByteString;
import com.mingle.MingleUserDto;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import io.quarkus.security.jpa.Password;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@Data
@UserDefinition
public class MingleUser extends PanacheEntity {

    @Embedded
    Audit audit;

    @Column(length = 500)
    private String bio;

    @Lob
    private byte[] image;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(unique = true, nullable = false)
    @Username
    private String username;

    @Column(nullable = false)
    @Password
    private String password;

    private String zip;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    private RELATIONSHIP relationship;

    @Enumerated(EnumType.STRING)
    private GENDER gender;

    @Enumerated(EnumType.STRING)
    private SKILL skill;

    private LocalDate birthday;

    @Roles
    private Set<String> roles;

    public MingleUser(MingleUserDto mingleUserDto){
        this.bio=mingleUserDto.getBio();
        this.image= mingleUserDto.getImage().toByteArray();
        this.firstname=mingleUserDto.getFirstname();
        this.lastname=mingleUserDto.getLastname();
        this.username=mingleUserDto.getUsername();
        this.password=mingleUserDto.getPassword();
        this.zip=mingleUserDto.getZip();
        this.email=mingleUserDto.getEmail();
        this.phone=mingleUserDto.getPhone();
        this.birthday= birthdayFormatter(mingleUserDto.getBirthday());
        this.relationship=RELATIONSHIP.valueOf(mingleUserDto.getRelationship());
        this.gender=GENDER.valueOf(mingleUserDto.getGender());
        this.skill = SKILL.valueOf(mingleUserDto.getSkill());
    }



    public Uni<MingleUser> persistWithHashedPwd(){
        if(this.image==null){
            setImage(new byte[0]);
        }
        setPassword(BCrypt.withDefaults().hashToString(12,this.password.toCharArray()));;
        return super.persist();
    }
    public MingleUserDto toMingleUserDto(){
        MingleUserDto.Builder builder =MingleUserDto.newBuilder()
                .setBio(this.bio)
                .setFirstname(this.firstname)
                .setLastname(this.lastname)
                .setUsername(this.username)
                .setZip(this.zip)
                .setEmail(this.email)
                .setPhone(this.phone)
                .setBirthday(String.valueOf(this.birthday))
                .setRelationship(String.valueOf(this.relationship))
                .setGender(String.valueOf(this.gender))
                .setSkill(String.valueOf(this.skill));
        Optional.ofNullable(this.image).ifPresent(t->builder.setImage(ByteString.copyFrom(t)));
        return builder.build();
    }
    public MingleUser updateMingleUser(MingleUserDto mingleUserDto){
                this.bio=mingleUserDto.getBio();
                this.firstname=mingleUserDto.getFirstname();
                this.lastname=mingleUserDto.getLastname();
                this.username=mingleUserDto.getUsername();
                this.zip=mingleUserDto.getZip();
                this.email=mingleUserDto.getEmail();
                this.phone=mingleUserDto.getPhone();
                this.birthday=birthdayFormatter(mingleUserDto.getBirthday());
                this.image=mingleUserDto.getImage().toByteArray();
                this.relationship=RELATIONSHIP.valueOf(mingleUserDto.getRelationship());
                this.gender=GENDER.valueOf(mingleUserDto.getGender());
                this.skill=SKILL.valueOf(mingleUserDto.getSkill());
                this.audit.setUpdatedDttm(LocalDateTime.now());
                this.audit.setUpdatedBy("mingle-be");
                return this;
    }
    public enum RELATIONSHIP {
        S,
        R
    }

    public enum GENDER {
        M,
        F
    }

    public enum SKILL {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED
    }
    private static LocalDate birthdayFormatter(String birthday){

        return LocalDate.parse(birthday, DateTimeFormatter.ofPattern("MM-dd-yyyy"));
    }


}