package com.mingle.entity;


import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import io.quarkus.security.jpa.Password;

@EqualsAndHashCode(callSuper = true)
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@Data
public class MingleUser extends PanacheEntity {

    @Embedded
    Audit audit;

    @Column(length = 500)
    private String bio;

    @Lob
    private byte[] profilePicture;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    @Password
    private String password;

    private Short zip;

    @Column(unique = true, nullable = false)
    private String email;

    private int phone;

    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    private RELATIONSHIP relationship;

    @Enumerated(EnumType.STRING)
    private GENDER gender;

    @Enumerated(EnumType.STRING)
    private SKILL skill;

    @Enumerated(EnumType.STRING)
    private AGE_RANGE ageRange;


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

    public enum AGE_RANGE {
        R1("18-35"),
        R2("35-50"),
        R3("50+");

        private final String range;

        AGE_RANGE(String range) {
            this.range = range;
        }

        public String getRange() {
            return range;
        }
    }
}