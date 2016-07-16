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
    private String referenceId;
    private String parentReferenceId;
    private String fileType; //PHOTO or VIDEO,Attachment
    private boolean thumbnail; // applicable to properties
    private String fileName;
    private String contentType;
    private String contentBase64;
    private String caption;

    //TODO: FileMetadata from GridFsFile

}
