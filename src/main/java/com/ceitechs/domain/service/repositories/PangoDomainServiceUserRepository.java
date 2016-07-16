package com.ceitechs.domain.service.repositories;

import java.math.BigInteger;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.ceitechs.domain.service.domain.User;

/**
 * <code>PangoDomainServiceUserRepository</code> - This is repository for the {@link User} document
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
public interface PangoDomainServiceUserRepository extends MongoRepository<User, BigInteger> {

}
