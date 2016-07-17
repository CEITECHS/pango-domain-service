package com.ceitechs.domain.service.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "review")
@TypeAlias("review")
@Getter
@Setter
@ToString
public class Review {
    @Id
    private String reviewReferenceId;
    @Indexed
    private String tenantReferenceId; // optional field, tenant being reviewed by landlord
    @Indexed
    private String propertyUnitReferenceId; // optional field, propertyUnit being reviewed by tenant
    private String reviewText;
    private double rating;
    private String reviewedBy;
    private LocalDateTime createdDate = LocalDateTime.now(Clock.systemUTC());
    private boolean recommend;
}
