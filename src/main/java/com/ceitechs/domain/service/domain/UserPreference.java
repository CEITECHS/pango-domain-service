package com.ceitechs.domain.service.domain;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

/**
 * @author iddymagohe
 * @since 0.1
 */
@Getter
@Setter
public class UserPreference {

    public enum PreferenceType {
        Notification,
        NotforDisplay
    }
    
    public enum PreferenceCategory {
        SEARCH,
        USERSET
    }

    private String preferenceId;
    private PreferenceType preferenceType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private boolean active;
    private boolean sendNotification;
    private PreferenceCategory category;
    private UserSearchHistory userSearchHistory;
}
