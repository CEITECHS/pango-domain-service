package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.domain.Address;
import com.ceitechs.domain.service.domain.Attachment;

/**
 * @author  by iddymagohe on 7/30/16.
 * @since 1.0 -
 */
public interface UserProjection {
    String getUserReferenceId();
    String getFullName();
    String getEmailAddress();
    String getPhoneNumber();
    Address getAddress();
    Attachment getProfilePicture();
}
