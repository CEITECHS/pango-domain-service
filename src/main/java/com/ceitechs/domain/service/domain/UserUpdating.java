package com.ceitechs.domain.service.domain;

/**
 * @author  iddymagohe on 8/6/16.
 */
public enum UserUpdating {
    /**
     * Strict profile picture change
     */
    PROFILE_PICTURE,
    /**
     * Strict password change
     */
    PASSWORD_CHANGE,
    /***
     * Basic information including but not limited to names, address , phone etc annotated by {@link com.ceitechs.domain.service.domain.Annotations.Updatable}.
     */
    BASIC_INFO
}
