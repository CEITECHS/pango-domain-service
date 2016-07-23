package com.ceitechs.domain.service.domain;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Transient;

import lombok.Getter;
import lombok.Setter;

/**
 * @author iddymagohe
 * @since 0.1
 */
@Getter
@Setter
public class UserProfile {
    private String password;
    @Transient
    private FileMetadata profilePicture;
    private boolean verified;
    private String verificationCode;
    private LocalDate verificationDate;
    private double customerRating;
    private LocalDate createdDate;
    @Transient Attachment attachmentPhoto;
}
