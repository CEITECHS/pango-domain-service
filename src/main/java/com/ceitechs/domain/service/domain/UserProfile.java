package com.ceitechs.domain.service.domain;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ceitechs.domain.service.service.AttachmentProjection;
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
    private boolean verified;
    private String verificationCode;
    private LocalDate verificationDate;
    private LocalDateTime passwordChangeDate;
    private double customerRating;
    private LocalDate createdDate;
    @Transient
    AttachmentProjection profilePicture;
    List<PangoUserRole> roles = new ArrayList<>();
}
