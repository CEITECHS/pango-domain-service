package com.ceitechs.domain.service.domain;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PropertyUnit {
    public enum PropertyPurpose {
        HOME,
        BUSINESS
    }
    private long propertyUnitId;
    private String propertyUnitDesc;
    private PropertyPurpose purpose;
    private ListingFor listingFor;
    private String propertyTerms;
    private LocalDate nextAvailableDate;
    private boolean autoListInd;
    private double[] location;
    private double propertyRating;
    private List<Review> reviews;
    private User owner;
    private Property property;
    private PropertyFeature features;
    private PropertyRent rent;
    private List<FileMetadata> propertyUnitImages;
    private List<UnitRentalHistory> history;
    private List<PropertyUnitEnquiry> enquiries;
    private List<UnitHoldingHistory> holdingHistory;
}