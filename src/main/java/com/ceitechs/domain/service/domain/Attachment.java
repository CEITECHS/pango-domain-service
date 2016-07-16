package com.ceitechs.domain.service.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @author iddymagohe
 * @since 0.1
 */

@Getter
@Setter
public class Attachment {

    private String fileType;
    private String fileName;
    private long fileSize;
    private String contentBase64;
    private String fileDescription;
    private boolean profilePicture;

}
