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
    private int resultCount;

    public UserSearchHistory(PropertySearchCriteria searchCriteria, int count) {
        query = searchCriteria;
        resultCount = count;
    }

    public UserSearchHistory() {
    }
}
