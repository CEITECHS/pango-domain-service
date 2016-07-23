package com.ceitechs.domain.service.service.events;

import com.ceitechs.domain.service.domain.PropertySearchCriteria;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.domain.UserSearchHistory;

/**
 * @author iddymagohe
 * @since 1.0
 */
public class OnPropertySearchEvent implements OnPangoEvent<UserSearchHistory> {

    private final UserSearchHistory userSearchHistory;

    private final User user;

    public OnPropertySearchEvent(UserSearchHistory userSearchHistory, User user) {
        this.userSearchHistory = userSearchHistory;
        this.user = user;
    }

    @Override
    public UserSearchHistory get() {
        return userSearchHistory;
    }

    @Override
    public User getUser() {
        return user;
    }
}
