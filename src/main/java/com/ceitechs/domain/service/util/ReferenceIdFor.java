package com.ceitechs.domain.service.util;

import lombok.Getter;

/**
 * @author iddymagohe
 * @since 1.0
 */

public enum ReferenceIdFor {
    USER(MetadataFields.USER_REFERENCE_ID),
    PROPERTY(MetadataFields.PROPERTY_REFERENCE_ID),
    UNIT_PROPERTY(MetadataFields.UNIT_REFERENCE_ID),
    ENQUIRY(MetadataFields.ENQUIRY_REFERENCE_ID);

    @Getter
    private String metadataField;

    ReferenceIdFor(String metadataField) {
        this.metadataField = metadataField;
    }
}
