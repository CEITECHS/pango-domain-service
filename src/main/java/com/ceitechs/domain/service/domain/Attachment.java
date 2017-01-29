package com.ceitechs.domain.service.domain;

import lombok.Getter;
import lombok.Setter;
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
@Document(collection = "attachments")
@TypeAlias("attachments")
public class Attachment{

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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Attachment{");
        sb.append("referenceId='").append(referenceId).append('\'');
        sb.append(", parentReferenceId='").append(parentReferenceId).append('\'');
        sb.append(", category='").append(category).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", thumbnail=").append(thumbnail);
        sb.append(", active=").append(active);
        sb.append(", userReferenceId='").append(userReferenceId).append('\'');
        sb.append(", bucket='").append(bucket).append('\'');
        sb.append(", url='").append(url).append('\'');
        sb.append(", createdDate=").append(createdDate);
        sb.append('}');
        return sb.toString();
    }
}
