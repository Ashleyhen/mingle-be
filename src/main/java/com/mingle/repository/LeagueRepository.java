package com.mingle.repository;

import com.mingle.entity.MingleLeague;
import com.mingle.entity.MingleUser;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LeagueRepository implements PanacheRepository<MingleLeague> {

    public Uni<MingleLeague> findByEventNameAndGroupId(String eventName, Long groupId) {
        return MingleLeague.find("""
                        SELECT l FROM MingleLeague l
                         JOIN l.mingleGroup g WHERE l.eventName = ?1 AND g.id = ?2""",
                        eventName, groupId)
                .firstResult();
    }
}
