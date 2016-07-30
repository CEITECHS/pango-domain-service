package com.ceitechs.domain.service.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * @author iddymagohe
 * @since 0.1
 */

@Getter
@Setter
public class Attachment implements Cloneable{

    private String fileType;
    private String fileName;
    private long fileSize;
    private String contentBase64;
    private String fileDescription;
    private boolean profilePicture;

    public String extractExtension(){
        if(getFileName().contains(".")){
            return getFileName().substring(getFileName().lastIndexOf(".")+1);
        }
        return "";
    }

    @Override
    public Attachment clone() throws CloneNotSupportedException {
        return (Attachment)super.clone();
    }

    public Attachment() {
    }

    public Attachment(FileMetadata fileMetadata) {
        if (fileMetadata != null) {
            this.setContentBase64(fileMetadata.getContentBase64());
            this.setFileType(fileMetadata.getFileType());
            this.setFileDescription(fileMetadata.getCaption());
            this.setProfilePicture(fileMetadata.isThumbnail());
        }
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "fileType='" + fileType + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", fileDescription='" + fileDescription + '\'' +
                ", profilePicture=" + profilePicture +
                '}';
    }
}
