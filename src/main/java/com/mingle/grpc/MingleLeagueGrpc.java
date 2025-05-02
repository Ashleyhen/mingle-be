package com.mingle.grpc;

import com.mingle.LeagueGrpc;
import com.mingle.MingleGroupDto;
import com.mingle.MingleId;
import com.mingle.MingleLeagueDto;
import com.mingle.impl.MingleCrudImpl;
import com.mingle.services.GroupService;
import com.mingle.services.LeagueService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@GrpcService
public class MingleLeagueGrpc implements LeagueGrpc {

    private final LeagueService leagueService;
    private MingleCrudImpl<MingleLeagueDto> leagueServiceImpl ;

    @PostConstruct
    void init(){
        leagueServiceImpl= new MingleCrudImpl<>(leagueService);
    }

    @Override
    public Uni<MingleLeagueDto> createLeague(MingleLeagueDto request) {
        return leagueServiceImpl.create(request).onItem().castTo(MingleLeagueDto.class);
    }
}
