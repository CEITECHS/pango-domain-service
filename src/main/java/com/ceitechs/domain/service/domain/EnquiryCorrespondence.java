package com.ceitechs.domain.service.domain;


import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

/**
 * @author iddymagohe
 * @since 0.1
 */
@Getter
@Setter
public class EnquiryCorrespondence {
    private String subject;
    private String message;
    private FileMetadata attachment;
    private LocalDate correspondenceDate;
    private CorrespondenceType correspondenceType;
    private boolean owner;
}
