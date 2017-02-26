package com.ceitechs.domain.service.domain;

import com.ceitechs.domain.service.service.AttachmentProjection;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Objects;

/**
 * @author iddymagohe on 12/18/16.
 */

@Getter
@Setter
@ToString
@Document(collection = "attachments")
@TypeAlias("attachments")
public class Attachment implements AttachmentProjection {

    @Id
    private String referenceId;

    @Indexed
    @NotEmpty(message = "attachment-parentReferenceId can not be null or Empty")
    private String parentReferenceId;

    @Indexed
    @NotEmpty(message = "attachment-category can not be null or Empty")
    private String category;

    private String description;

    private boolean thumbnail = false;

    private boolean active = true;

    @JsonIgnore
    @Transient
    private MultipartFile attachment;

    // uploaded by
    @Indexed
    @NotEmpty(message = "user-referenceId can not be null or Empty")
    private String userReferenceId;

    //directory stored
    private String bucket;

    // accessible url to an attachment
    @Transient
    private String url;

    private LocalDate createdDate = LocalDate.now();

    public enum attachmentCategoryType {
        PROPERTY,
        PROFILE_PICTURE,
        CORRESPONDENCE,
        OTHER
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attachment that = (Attachment) o;
        return Objects.equals(referenceId, that.referenceId) &&
                Objects.equals(parentReferenceId, that.parentReferenceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referenceId, parentReferenceId);
    }
}
