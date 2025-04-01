package com.mingle.services;


import com.mingle.CredentialsDto;
import com.mingle.MingleUserDto;
import com.mingle.entity.MingleUser;
import com.mingle.exception.CreationException;
import io.quarkus.runtime.util.StringUtil;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;

import java.util.*;
import java.util.stream.Collectors;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final ModelMapper modelMapper = new ModelMapper();

    public Uni<MingleUserDto> AuthenticateWithEmailAndPassword(CredentialsDto credentials) {

        return Uni.createFrom().item(() -> {
            Optional<MingleUser> optionalUser = MingleUser.find("email = ?1 and password = ?2", credentials.getEmail(), credentials.getPassword())
                    .project(MingleUser.class)
                    .firstResultOptional();
            return optionalUser
                    .map(t -> MingleUserDto
                            .newBuilder(
                                    modelMapper.map(t, MingleUserDto.class)
                            ).setPassword("").build()
                    )
                    .orElseThrow(() -> {
                        log.error("UNAUTHORIZED");
                        return new UnauthorizedException("UNAUTHORIZED");
                    });
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool()); // Offload to worker thread
    }


    @Transactional
    public Uni<Long> createUser(MingleUserDto mingleUserDto) {
        return Uni.createFrom().item(() -> {
            Optional<MingleUser> duplicate = MingleUser.find(
                            "email =?1 or username =?2", mingleUserDto.getEmail(), mingleUserDto.getUsername()
                    )
                    .project(MingleUser.class).firstResultOptional();
            if (duplicate.isPresent()) {
                log.error("duplicate user found check for user id {}", duplicate.get().id);
                throw new CreationException.DuplicateUser(duplicate.get().getUsername(), duplicate.get().getEmail());
            }
            MingleUser mingleUser = modelMapper.map(validation(mingleUserDto), MingleUser.class);
            mingleUser.setIsActive(false);
            mingleUser.persist();
            return mingleUser.id;
        }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool()); // Offload to worker thread
    }

    private MingleUserDto validation(MingleUserDto mingleUser) {

        String errorsMsg = Map.of(
                        MingleUser.Fields.email, mingleUser.getEmail(),
                        MingleUser.Fields.firstname, mingleUser.getFirstname(),
                        MingleUser.Fields.lastname, mingleUser.getLastname(),
                        MingleUser.Fields.password, mingleUser.getPassword(),
                        MingleUser.Fields.phone, mingleUser.getPhone(),
                        MingleUser.Fields.zip, mingleUser.getZip(),
                        MingleUser.Fields.ageRange, mingleUser.getAgeRange(),
                        MingleUser.Fields.gender, mingleUser.getGender()
                ).entrySet().stream().filter(t -> StringUtil.isNullOrEmpty(t.getValue()))
                .map(t -> t.getKey() + " is required!").collect(Collectors.joining());

        if (StringUtil.isNullOrEmpty(errorsMsg)) {
            throw new CreationException.MissingField(errorsMsg);
        }

        return mingleUser;
    }
}
