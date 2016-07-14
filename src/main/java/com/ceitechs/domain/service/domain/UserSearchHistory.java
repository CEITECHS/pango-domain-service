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
public class UserSearchHistory {
    private LocalDate date;
    private String query;
}
