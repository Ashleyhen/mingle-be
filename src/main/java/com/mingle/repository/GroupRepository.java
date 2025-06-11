package com.mingle.repository;

import com.mingle.entity.MingleGroup;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GroupRepository implements
        PanacheRepository<MingleGroup>,
        IFindWithUser<MingleGroup>
{

    public Uni<MingleGroup> findByGroupNameAndZip(String groupName, String zip){
        return  MingleGroup.find("groupName = ?1 and zip =?2", groupName,zip).firstResult();
    }
//    this doesn't look correct

    @Override
    public Uni<MingleGroup> findByIdWithOrganizer(String sub) {
        return find("SELECT mg FROM MingleGroup mg JOIN FETCH mg.organizer WHERE mg.sub = ?1", sub).firstResult();
    }
}
