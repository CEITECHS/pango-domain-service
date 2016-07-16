package com.ceitechs.domain.service.domain;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * @author iddymagohe
 * @since 0.1
 */

@Getter
@Setter
public class PropertyUnitEnquiry {
    private String correspondenceId;
    private User user;
    private PropertyUnit propertyUnit;
    private String subject;
    private String introduction;
    private String message;
    private LocalDate enquiryDate;
    private CorrespondenceType enquiryType;
    List<EnquiryCorrespondence> correspondences;
}
