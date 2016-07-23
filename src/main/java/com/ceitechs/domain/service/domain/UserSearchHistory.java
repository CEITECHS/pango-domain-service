package com.ceitechs.domain.service.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * @author iddymagohe
 * @since 0.1
 */
@Getter
@Setter
public class UserSearchHistory {
    private LocalDateTime date = LocalDateTime.now(Clock.systemUTC());
    private PropertySearchCriteria query;

    public UserSearchHistory(PropertySearchCriteria searchCriteria) {
        query = searchCriteria;
    }
}
