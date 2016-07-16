package com.ceitechs.domain.service.domain;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "review")
@TypeAlias("review")
@Getter
@Setter
public class Review {
    @Id
    private String reviewReferenceId;
    private User tenant; // optional field, tenant being reviewed by landlord
    private PropertyUnit propertyUnit; //// optional field, propertyUnit being reviewed by tenant
    private String reviewText;
    private double rating;
    private String reviewedBy;
    private LocalDate createdDate;
    private boolean recommend;
}
