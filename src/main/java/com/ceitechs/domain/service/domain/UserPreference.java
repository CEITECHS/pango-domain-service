package com.ceitechs.domain.service.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author iddymagohe
 * @since 0.1
 */
@Getter
@Setter
@ToString
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
    private LocalDateTime createdOn = LocalDateTime.now();
    private LocalDate fromDate;
    private LocalDate toDate;
    private boolean active;
    private boolean sendNotification;
    private PreferenceCategory category;
    private UserSearchHistory userSearchHistory;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserPreference that = (UserPreference) o;

        return preferenceId.equals(that.preferenceId);

    }

    @Override
    public int hashCode() {
        return preferenceId.hashCode();
    }


}
