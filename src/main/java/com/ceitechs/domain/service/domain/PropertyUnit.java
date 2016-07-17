package com.ceitechs.domain.service.domain;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "propertyunit")
@TypeAlias("propertyunit")
public class PropertyUnit {
    public enum PropertyPurpose {
        HOME,
        BUSINESS
    }

    @Id
    private String propertyUnitId;

    private String propertyUnitDesc;

    private PropertyPurpose purpose;

    private ListingFor listingFor;

    private String propertyTerms;

    private LocalDate nextAvailableDate;

    private boolean autoListInd;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
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