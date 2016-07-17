package com.ceitechs.domain.service.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
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

    private LocalDateTime nextAvailableDate;

    private boolean autoListInd;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private double[] location;

    private double propertyRating;

    @DBRef
    private User owner;

    private Property property;

    private PropertyFeature features;

    private PropertyRent rent;

    @Transient
    private List<FileMetadata> propertyUnitImages;

    private List<UnitRentalHistory> history;

    private List<PropertyUnitEnquiry> enquiries;

    private List<UnitHoldingHistory> holdingHistory;
}