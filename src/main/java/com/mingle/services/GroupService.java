package com.mingle.services;

import com.mingle.*;
import com.mingle.entity.MingleGroup;
import com.mingle.exception.DuplicateException;
import com.mingle.exception.NotFoundException;
import com.mingle.impl.IMingleCreate;
import com.mingle.repository.GroupRepository;
import com.mingle.repository.MingleUserRepository;
import com.mingle.security.AuthenticateUser;
import com.mingle.utility.ValidateParams;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.reactive.mutiny.Mutiny;

import static com.mingle.utility.ValidateParams.mingleFieldValidation;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class GroupService  implements IMingleCreate<MingleGroupDto> {

    private final GroupRepository groupRepository;

    private final AuthenticateUser authenticateUser;

    private final MingleUserRepository mingleUserRepository;

    @WithTransaction
    public Uni<MingleGroupDto> createGroup(MingleGroupDto mingleGroup) {
        log.info("Creating group: {}", mingleGroup);
        return validateParams(mingleGroup)
                .chain(() -> checkForDuplicates(mingleGroup))
                .chain(() -> createNewMingleEntity(mingleGroup));
    }


    @WithTransaction
    public Uni<MingleGroupDto> updateGroup(MingleGroupDto mingleGroupDto) {
        return authenticateUser.getAuthorizedUser(groupRepository)
                .onItem().ifNull().failWith(new NotFoundException(
                        "Failed to update group",
                        "Can't update group if the group doesn't exist",
                        "missing mingle group with the mingleGroupDto.id "+mingleGroupDto.getId()
                                +"group name:"+ mingleGroupDto.getGroupName()+" zip:"+mingleGroupDto.getZip()))
                .invoke(()-> checkForExistingGroupBeforeUpdating(mingleGroupDto))
                .chain((groupEntity)-> persistGroup(mingleGroupDto, groupEntity));
    }

    @WithTransaction
    public Uni<ListMingleGroupDto> findAllGroupsByUserId(String sub) {
        return mingleUserRepository.findById(sub)
                .flatMap(user -> Mutiny.fetch(user.getMingleGroup()) // Fetch the lazy collection correctly
                        .map(groups -> ListMingleGroupDto.newBuilder()
                                .addAllGroup(groups.stream().map(MingleGroup::toMingleGroupDto).toList())
                                .build())
                );
    }

    private static Uni<MingleGroupDto> persistGroup(MingleGroupDto mingleGroup, MingleGroup groupEntity) {
        groupEntity.setGroupName(mingleGroup.getGroupName());
        groupEntity.setImages(mingleGroup.getImages().toByteArray());
        groupEntity.getAudit().setUpdatedBy(groupEntity.getOrganizer().getUsername());
        groupEntity.setDescription(groupEntity.getDescription());
        groupEntity.setZip(groupEntity.getZip());
        return groupEntity.persist()
                .onItem().castTo(MingleGroup.class)
                .chain((group) ->
                        Uni.createFrom()
                                .item(group::toMingleGroupDto));
    }

    private Uni<MingleGroup> checkForExistingGroupBeforeUpdating(MingleGroupDto mingleGroup) {
        return groupRepository.findByGroupNameAndZip(mingleGroup.getGroupName(), mingleGroup.getZip())
                .onItem().ifNotNull().invoke((groupEntity -> {
                    if (groupEntity.id != mingleGroup.getId()) {
                        throw new DuplicateException(
                                "Can't update an existing group!",
                                "that group name already exist within that zip code",
                                mingleGroup
                        );

                    }
                }));
    }



    @Override
    @WithTransaction
    public Uni<MingleGroupDto> createNewMingleEntity(MingleGroupDto mingleGroup) {
        return authenticateUser.getAuthorizedUser()
                .flatMap(mingleUserRepository::findById)
                .flatMap(mingleUser->{
            MingleGroup newGroup = new MingleGroup(mingleGroup); // Convert DTO to entity
            newGroup.setIsActive(true);
            newGroup.setOrganizer(mingleUser);
            return newGroup.persist()
                    .onItem().castTo(MingleGroup.class)
                    .map(savedGroup -> {
                        log.info("Group successfully created: ID={}", savedGroup.id);
                        return savedGroup.toMingleGroupDto();
                    });
        });

    }



    @Override
    public  Uni<MingleGroupDto> validateParams(MingleGroupDto mingleGroupDto) {
        return Uni.createFrom().item(() -> {
            mingleFieldValidation(
                    ValidateParams.zip(mingleGroupDto.getZip()),
                    ValidateParams.groupName(mingleGroupDto.getGroupName())
            );
            return mingleGroupDto; // Validation pass: no errors
        });
    }

    @Override
    @WithTransaction
    public Uni<MingleGroup> checkForDuplicates(MingleGroupDto mingleUserDto) {
        return groupRepository.findByGroupNameAndZip(mingleUserDto.getGroupName(), mingleUserDto.getZip())
                .onItem().ifNotNull().failWith(() -> {
                    throw new DuplicateException(
                            "Duplicate group found!",
                            "Group Already Exist. Try using a different group name or a different ZIP.",
                            mingleUserDto
                    );
                });
    }


}
