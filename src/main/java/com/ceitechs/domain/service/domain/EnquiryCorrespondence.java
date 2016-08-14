package com.ceitechs.domain.service.domain;


import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

/**
 * @author iddymagohe
 * @since 0.1
 */
@Getter
@Setter
@ToString
public class EnquiryCorrespondence {
    private long correspondenceReferenceId;
    private String message;
    @Transient
    private Attachment attachment;
    private LocalDateTime correspondenceDate = LocalDateTime.now(Clock.systemUTC());
    private CorrespondenceType correspondenceType;
    private boolean owner;
    @Transient
    String attachmentId;
}
