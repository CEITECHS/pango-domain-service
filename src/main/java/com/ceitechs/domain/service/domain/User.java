package com.ceitechs.domain.service.domain;

import java.util.*;

import com.ceitechs.domain.service.domain.Annotations.Updatable;
import com.ceitechs.domain.service.service.UserProjection;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
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

    @Transient
    private double longitude; // when passed used to calculate distance btn user and property
    @Transient
    private double latitude; //when passed used to calculate distance btn user and property

    private List<UserPreference> preferences = new ArrayList<>();

    private Set<String> favouredProperties = new HashSet<>();

    @Transient
    private String verificationPathParam;

    @Override
    public String getFullName() {
        return firstName + " " + lastName;
    }



    @Override
    public Attachment getProfilePicture() {
        return profile.getProfilePicture();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (userReferenceId != null ? !userReferenceId.equals(user.userReferenceId) : user.userReferenceId != null)
            return false;
        return emailAddress != null ? emailAddress.equals(user.emailAddress) : user.emailAddress == null;

    }

    @Override
    public int hashCode() {
        int result = userReferenceId != null ? userReferenceId.hashCode() : 0;
        result = 31 * result + (emailAddress != null ? emailAddress.hashCode() : 0);
        return result;
    }
}
