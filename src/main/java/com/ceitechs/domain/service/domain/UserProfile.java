package com.ceitechs.domain.service.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * @author iddymagohe
 * @since 0.1
 */
@Getter
@Setter
public class UserProfile {
    private long profileId;
    private String userName;
    private String password;
    private Picture profilePicture;
    private boolean verified;
    private String verificationCode;
    private LocalDate verificationDate;
    private double customerRating;
    private LocalDate createdDate;
    private List<UserPreference> preferences;
}
