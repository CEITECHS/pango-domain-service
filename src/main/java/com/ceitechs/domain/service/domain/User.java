package com.ceitechs.domain.service.domain;

import java.math.BigInteger;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

/**
 * @author abhisheksingh
 * @since 0.1
 */
@Getter
@Setter
@Document(collection="user")
public class User {
    @Id
    private BigInteger userReferenceId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String emailAddress;
    private Address address;
    private UserProfile profile;
    private List<UserPreference> preferences;
}
