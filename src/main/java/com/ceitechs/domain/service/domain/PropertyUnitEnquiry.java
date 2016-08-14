package com.ceitechs.domain.service.domain;


import com.ceitechs.domain.service.service.EnquiryProjection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.Assert;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author iddymagohe
 * @since 0.1
 */

@Getter
@Setter
@ToString
@Document(collection = "property_unit_enquiry")
@TypeAlias(value = "property_unit_enquiry")
public class PropertyUnitEnquiry implements EnquiryProjection {
    @Id
    private String enquiryReferenceId;
    @DBRef
    private User prospectiveTenant;
    @DBRef
    private PropertyUnit propertyUnit;
    private String ownerReferenceId;
    private String subject;
    private String introduction;
    private String message;
    private LocalDateTime enquiryDate = LocalDateTime.now(Clock.systemUTC());
    private CorrespondenceType enquiryType;
    List<EnquiryCorrespondence> correspondences = new ArrayList<>();

    public void addCorrespondence(EnquiryCorrespondence e){
        Assert.notNull(e, "Correspondence object can not be null");
         correspondences.add(e);
    }

    @Override
    public int getCorrespondenceCount() {
        return correspondences.size();
    }
}
