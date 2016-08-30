package com.ceitechs.domain.service.domain;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ceitechs.domain.service.service.PropertyProjection;
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
public class PropertyUnit  implements PropertyProjection {
    public enum PropertyPurpose {
        HOME,
        BUSINESS
    }

    @Id
    private String propertyId;

    private String unitNumber;

    private String propertyUnitDesc;

    private PropertyPurpose purpose;

    private ListingFor listingFor;

    private String propertyTerms;

    private LocalDateTime nextAvailableDate;

    private boolean autoListInd;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private double[] location; //location(long,lat)
    private Address address;

    private double propertyRating;

    @DBRef
    private User owner;

    private PropertyFeature features;

    private PropertyRent rent;

    @Transient
    private List<FileMetadata> propertyUnitImages = new ArrayList<>();
    @Transient
    private List<Attachment> attachments = new ArrayList<>();

    @Transient
    private Attachment coverPhoto;

    @Transient
    private List<UnitRentalHistory> rentingHistory;

    @Transient
    private List<PropertyUnitEnquiry> enquiries;

    @Transient
    private List<PropertyHoldingHistory> holdingHistory;

    @Transient
    private double distance;

    private boolean active = true;

    private LocalDateTime createdDate = LocalDateTime.now(Clock.systemUTC());


}