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
    public Uni<MingleUser> findByEmailOrPassword(String email,String password){
        return  MingleUser.find("email = ?1 or password =?2", email,password).firstResult();
    }
}
