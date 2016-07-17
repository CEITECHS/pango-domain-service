package com.ceitechs.domain.service.domain;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author iddymagohe
 * @since 0.1
 */

@Getter
@Setter
@Document(collection = "property_unit_enquiry")
@TypeAlias(value = "property_unit_enquiry")
public class PropertyUnitEnquiry {
    @Id
    private String enquiryReferenceId;
    @DBRef
    private User prospectiveTenant;
    @DBRef
    private PropertyUnit propertyUnit;
    private String subject;
    private String introduction;
    private String message;
    private LocalDateTime enquiryDate = LocalDateTime.now(Clock.systemUTC());
    private CorrespondenceType enquiryType;
    List<EnquiryCorrespondence> correspondences;
}
