package com.mingle.services;

import com.mingle.MingleLeagueDto;
import com.mingle.entity.MingleGroup;
import com.mingle.entity.MingleLeague;
import com.mingle.exception.DuplicateException;
import com.mingle.impl.IMingleCreate;
import com.mingle.repository.GroupRepository;
import com.mingle.repository.LeagueRepository;
import com.mingle.security.AuthenticateUser;
import com.mingle.utility.ValidateParams;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static com.mingle.utility.ValidateParams.mingleFieldValidation;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class LeagueService implements IMingleCreate<MingleLeagueDto> {

    private final LeagueRepository leagueRepository;
    private final GroupRepository groupRepository;
    @Inject
    private final AuthenticateUser authenticateUser;

    @Override
    @WithTransaction
    public Uni<MingleLeagueDto> validateParams(MingleLeagueDto leagueDto) {
       return Uni.createFrom().item(() -> {
            mingleFieldValidation(
                ValidateParams.eventName(leagueDto.getEventName()),
                ValidateParams.mingleDateFormat(leagueDto.getStartDate()),
                ValidateParams.mingleDateFormat(leagueDto.getEndDate()),
                Optional.of(leagueDto).map(MingleLeagueDto::getRegistrationEndDate).flatMap(ValidateParams::mingleDateFormat)
            );
            return leagueDto;
        });
    }

    @Override
    @WithTransaction
    public Uni<MingleLeague> checkForDuplicates(MingleLeagueDto mingleLeagueDto) {
        return leagueRepository.findByEventNameAndGroupId(mingleLeagueDto.getEventName(),mingleLeagueDto.getMingleGroupDto().getId())
                .onItem().ifNotNull().failWith(()->new DuplicateException(
                        "Failed to create league",
                        "League name already exist for the current group",
                        mingleLeagueDto
                ));
    }

    @Override
    @WithTransaction
    public Uni<MingleLeagueDto> createNewMingleEntity(MingleLeagueDto mingleLeagueDto) {
        return authenticateUser.getAuthorizedUser(groupRepository).chain(
                mingleGroup -> {
                    MingleLeague mingleLeague=new MingleLeague(mingleLeagueDto);
                    mingleLeague.setMingleGroup(mingleGroup);
                    return mingleLeague.persist();
                }).onItem()
                .castTo(MingleLeague.class).map(mingleLeague->{
                    log.info("League successfully created: ID={}",mingleLeague.id);
                    return mingleLeague.getMingleLeagueDto();
                });
    }
}
