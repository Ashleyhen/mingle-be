package com.mingle.grpc;

import com.mingle.LeagueGrpc;
import com.mingle.LocationGrpc;
import com.mingle.MingleLeagueDto;
import com.mingle.MingleLocationDto;
import com.mingle.entity.MingleLocation;
import com.mingle.impl.MingleCrudImpl;
import com.mingle.services.LeagueService;
import com.mingle.services.LocationService;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@GrpcService
public class MingleLocationGrpc implements LocationGrpc {
    private final LocationService leagueService;
    private MingleCrudImpl<MingleLocationDto> locationServiceImpl ;

    @PostConstruct
    void init(){
        locationServiceImpl= new MingleCrudImpl<>(leagueService);
    }



    @Override
    public Uni<MingleLocationDto> createLocation(MingleLocationDto request) {
        return locationServiceImpl.create(request);
    }
}
