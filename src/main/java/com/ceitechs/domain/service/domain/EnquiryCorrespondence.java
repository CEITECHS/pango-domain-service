package com.ceitechs.domain.service.domain;


import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import com.ceitechs.domain.service.service.AttachmentProjection;
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
    private LocalDateTime correspondenceDate = LocalDateTime.now(Clock.systemUTC());
    private CorrespondenceType correspondenceType;
    private boolean owner;
}
