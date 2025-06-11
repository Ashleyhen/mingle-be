package com.mingle.security;

import com.mingle.entity.MingleUser;
import com.mingle.exception.MingleAuthenticationException;
import com.mingle.repository.IFindWithUser;
import com.mingle.repository.MingleUserRepository;
import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Singleton
public class AuthenticateUser {

    private final MingleUserRepository mingleUserRepository;

    @Inject
    private CurrentIdentityAssociation currentIdentityAssociation;

    public Uni<String> getAuthorizedUser() {
        return currentIdentityAssociation.getDeferredIdentity()
                .onItem().transform(securityIdentity -> {
                    if (securityIdentity.isAnonymous()) {
                        throw new MingleAuthenticationException("missing security identity");
                    }
                    return  securityIdentity.getAttribute("sub").toString();
                });
    }

    public <T extends PanacheEntity> Uni<T> getAuthorizedUser(IFindWithUser<T> findWithUser) {
        return this.getAuthorizedUser()
                .flatMap(findWithUser::findByIdWithOrganizer);
    }
}
