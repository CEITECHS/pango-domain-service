package com.ceitechs.domain.service.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ceitechs.domain.service.domain.Annotations.Updatable;
import com.ceitechs.domain.service.service.UserProjection;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author abhisheksingh
 * @since 0.1
 */
@Getter
@Setter
@ToString
@Document(collection = "user")
@TypeAlias("user")
public class User implements UserProjection {
    @Id
    private String userReferenceId;

    @Updatable
    private String firstName;

    @Updatable
    private String lastName;

    @Updatable
    private String phoneNumber;

    @Updatable
    private String emailAddress;

    @Updatable
    private Address address;

    private UserProfile profile;

    private List<UserPreference> preferences = new ArrayList<>();

    @Override
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public Attachment getProfilePicture() {
        return profile.getProfilePicture();
    }
}
