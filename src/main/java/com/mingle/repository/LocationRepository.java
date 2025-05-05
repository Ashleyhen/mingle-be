package com.mingle.repository;

import com.mingle.MingleLocationDto;
import com.mingle.entity.MingleGroup;
import com.mingle.entity.MingleLeague;
import com.mingle.entity.MingleLocation;
import com.mingle.entity.MingleUser;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LocationRepository implements PanacheRepository<MingleLocation> {

    public Uni<MingleLocation> findByLocationNameAndLeague(String locationName, Long leagueId) {
        return MingleLocation.find("""
                        SELECT l FROM MingleLocation l
                         JOIN FETCH l.mingleLeague ml
                          WHERE l.locationName = :locationName AND ml.id = :leagueId""",
                        Parameters.with("locationName", locationName)
                                .and("leagueId", leagueId))
                .firstResult();
    }
}
