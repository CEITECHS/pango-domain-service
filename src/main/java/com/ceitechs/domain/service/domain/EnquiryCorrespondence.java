package com.ceitechs.domain.service.domain;


import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

/**
 * @author iddymagohe
 * @since 0.1
 */
@Getter
@Setter
public class EnquiryCorrespondence {
    private long correspondenceReferenceId;
    private String message;
    @Transient
    private FileMetadata attachment;
    private LocalDate correspondenceDate;
    private CorrespondenceType correspondenceType;
    private boolean owner;
}
