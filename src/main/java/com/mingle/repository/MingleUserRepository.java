package com.mingle.repository;

import com.mingle.entity.MingleUser;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class MingleUserRepository implements PanacheRepository<MingleUser> {

    public Uni<MingleUser> findByEmail(String email){
        return  MingleUser.find("email = ?1", email).firstResult();
    }

    public Uni<MingleUser> findByEmailOrUsername(String email, String username){
        return  MingleUser.find("email = ?1 or username =?2", email,username).firstResult();
    }
}
