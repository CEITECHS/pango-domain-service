package com.ceitechs.domain.service.util;

import lombok.Getter;

import java.util.Optional;

/**
 * @author iddymagohe
 * @since 1.0
 */

public enum ReferenceIdFor {
    USER(MetadataFields.USER_REFERENCE_ID, Optional.empty()),
    PROPERTY(MetadataFields.PROPERTY_REFERENCE_ID,Optional.empty()),
    UNIT_PROPERTY(MetadataFields.UNIT_REFERENCE_ID, Optional.of(ReferenceIdFor.PROPERTY)),
    ENQUIRY(MetadataFields.ENQUIRY_REFERENCE_ID,Optional.of(ReferenceIdFor.UNIT_PROPERTY));

    @Getter
    private String metadataField;

    @Getter
    private Optional<ReferenceIdFor> parentField;

    ReferenceIdFor(String metadataField, Optional<ReferenceIdFor> parentField) {
        this.metadataField = metadataField;
        this.parentField = parentField;
    }
}
