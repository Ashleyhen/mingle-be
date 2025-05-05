package com.mingle.services;

import com.google.protobuf.GeneratedMessageV3;
import com.mingle.MingleLeagueDto;
import com.mingle.MingleLocationDto;
import com.mingle.entity.MingleLeague;
import com.mingle.entity.MingleLocation;
import com.mingle.exception.DuplicateException;
import com.mingle.impl.IMingleCreate;
import com.mingle.repository.GroupRepository;
import com.mingle.repository.LeagueRepository;
import com.mingle.repository.LocationRepository;
import com.mingle.utility.ValidateParams;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.mingle.utility.ValidateParams.mingleFieldValidation;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class LocationService implements IMingleCreate<MingleLocationDto> {
    private final LocationRepository locationRepository;
    private final LeagueRepository leagueRepository;
    @Override
    public Uni<MingleLocationDto> validateParams(MingleLocationDto leagueDto) {
        return Uni.createFrom().item(() -> {
            mingleFieldValidation(
                ValidateParams.email(leagueDto.getHostEmail()),
                ValidateParams.firstName(leagueDto.getHostName())
            );
            return leagueDto;
        });
    }

    @Override
    @WithTransaction
    public Uni<MingleLocation> checkForDuplicates(MingleLocationDto mingleLocationDto) {
        return locationRepository.findByLocationNameAndLeague(
                mingleLocationDto.getLocationName(),
                mingleLocationDto.getMingleLeagueDto().getId())
                .onItem().ifNotNull().failWith(
                        new DuplicateException("Failed to create a location",
                                "A location with that name under the league " +mingleLocationDto.getMingleLeagueDto()+" already exist.",
                                mingleLocationDto)
                );
    }

    @Override
    @WithTransaction
    public Uni<MingleLocationDto> createNewMingleEntity(MingleLocationDto mingleLocationDto) {
        return leagueRepository.findById(mingleLocationDto.getMingleLeagueDto().getId()).chain(mingleLeague->{
            MingleLocation mingleLocation = new MingleLocation(mingleLocationDto);
            mingleLocation.setMingleLeague(mingleLeague);
            return mingleLocation.persist();
        }).onItem()
                .castTo(MingleLocation.class)
                .map(mingleLocation -> {
                    log.info("Location successfully created: ID={}", mingleLocation.id);
                    return mingleLocation.getMingleLocationDto();
                });
    }
}
