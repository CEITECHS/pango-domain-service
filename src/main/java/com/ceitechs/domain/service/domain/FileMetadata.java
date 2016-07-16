package com.ceitechs.domain.service.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @author iddymagohe
 * @since 0.1
 */
@Getter
@Setter
public class FileMetadata {

    public enum FILETYPE{
        PHOTO,
        DOCUMENT,
        VIDEO
    }

    @Getter
    private String referenceId;
    @Getter
    private String fileType; //PHOTO or VIDEO,Attachment
    @Getter
    private boolean thumbnail; // applicable to properties
    @Getter
    private String fileName;
    @Getter
    private String contentType;
    @Getter
    private String contentBase64;
    @Getter
    private String fileDescription;

}
