package com.mingle.services;

import com.mingle.*;
import com.mingle.entity.Audit;
import com.mingle.entity.MingleGroup;
import com.mingle.entity.MingleUser;
import com.mingle.exception.DuplicateException;
import com.mingle.exception.NotFoundException;
import com.mingle.repository.GroupRepository;
import com.mingle.utility.ValidateParams;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;

import static com.mingle.utility.ValidateParams.mingleFieldValidation;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class GroupService  {
    private final GroupRepository groupRepository;

    @WithTransaction
    public Uni<GroupCreatedResponse> createGroup(MingleGroupDto mingleGroup) {
        log.info("Creating group: {}", mingleGroup);
        return validateParams(mingleGroup)
                .chain(() -> checkForDuplicateGroup(mingleGroup))
                .chain(() -> persistNewGroup(mingleGroup));
    }


    @WithTransaction
    public Uni<GroupUpdatedResponse> updateGroup(MingleGroupDto mingleGroupDto) {
        return validateParams(mingleGroupDto)
                .chain(()-> checkForExistingGroupBeforeUpdating(mingleGroupDto))
                .onItem().ifNull().switchTo(()->groupRepository.findById(mingleGroupDto.getId()))
                .onItem().ifNull().failWith(new DuplicateException("Failed to update user","Can't update user to an existing username or email",mingleGroupDto))
                .chain((groupEntity)-> persistGroup(mingleGroupDto, groupEntity));
    }


    private static Uni<GroupUpdatedResponse> persistGroup(MingleGroupDto mingleGroup, MingleGroup groupEntity) {
        groupEntity.setGroupName(mingleGroup.getGroupName());
        groupEntity.setImages(mingleGroup.getImages().toByteArray());
        groupEntity.getAudit().setUpdatedBy(groupEntity.getOrganizer().getUsername());
        groupEntity.setDescription(groupEntity.getDescription());
        groupEntity.setZip(groupEntity.getZip());
        return groupEntity.persist()
                .chain(() ->
                        Uni.createFrom()
                                .item(() -> GroupUpdatedResponse.newBuilder()
                                        .setResponse("Successfully updated Group")
                                        .build()));
    }

    private Uni<MingleGroup> checkForExistingGroupBeforeUpdating(MingleGroupDto mingleGroup) {
        return groupRepository.findByGroupNameAndZip(mingleGroup.getGroupName(), mingleGroup.getZip())
                .onItem().invoke((groupEntity -> {
                    if (groupEntity.id != mingleGroup.getId()) {
                        throw new DuplicateException(
                                "Can't update an existing group!",
                                "that group name already exist within that zip code",
                                mingleGroup
                        );

                    }
                }));
    }

    private Uni<Void> checkForDuplicateGroup(MingleGroupDto mingleGroup) {
        return groupRepository.findByGroupNameAndZip(mingleGroup.getGroupName(), mingleGroup.getZip())
                .onItem().ifNotNull().failWith(() -> {
                    throw new DuplicateException(
                            "Duplicate group found!",
                            "Group Already Exist. Try using a different group name or a different ZIP.",
                            mingleGroup
                    );
                }).replaceWithVoid();
    }

    private Uni<GroupCreatedResponse> persistNewGroup(MingleGroupDto mingleGroup) {
        MingleGroup newGroup = new MingleGroup(mingleGroup); // Convert DTO to entity
        newGroup.setIsActive(true);
        return newGroup.persist()
                .onItem().castTo(MingleGroup.class)
                .map(savedGroup -> {
                    log.info("Group successfully created: ID={}", savedGroup.id);
                    return GroupCreatedResponse.newBuilder()
                            .setId(savedGroup.id)
                            .build();
                });
    }



    private static Uni<Void> validateParams(MingleGroupDto mingleGroupDto) {
        return Uni.createFrom().item(() -> {
            mingleFieldValidation(
                    ValidateParams.zip(mingleGroupDto.getZip()),
                    ValidateParams.groupName(mingleGroupDto.getGroupName())
            );
            return null; // Validation pass: no errors
        });
    }
}
