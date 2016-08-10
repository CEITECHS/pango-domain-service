package com.ceitechs.domain.service.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ceitechs.domain.service.domain.User;

/**
 * <code>PangoDomainServiceUserRepository</code> - This is repository for the {@link User} document
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public interface UserRepository extends MongoRepository<User, String> , UserRepositoryCustom{

    User findByEmailAddressIgnoreCaseAndProfileVerifiedTrue(String email);

    User findByEmailAddressIgnoreCase(String emailAddress);

    User findByEmailAddressOrUserReferenceIdAllIgnoreCaseAndProfileVerifiedTrue(String emailAddress, String userReferenceId);


}

