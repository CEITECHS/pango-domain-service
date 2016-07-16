package com.ceitechs.domain.service.domain;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Review {
    private User user;
    private PropertyUnit propertyUnit;
    private long reviewId;
    private String reviewText;
    private double rating;
    private String reviewedBy;
    private LocalDate createdDate;
    private boolean recommend;
}
