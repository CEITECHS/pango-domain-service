package com.ceitechs.domain.service.domain;

import com.ceitechs.domain.service.util.MetadataFields;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

/**
 * @author iddymagohe
 * @since 0.1
 */

@Getter
@Setter
public class Attachment implements Cloneable{

    private String fileType; // Possible values are PHOTO, VIDEO, DOCUMENT
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
            this.setFileName(fileMetadata.getFileName());
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

    public String getFileType(){
            try {
                fileType = FileMetadata.FILETYPE.valueOf(fileType.toUpperCase()).name();
            }catch (Exception ex){
                fileType = FileMetadata.FILETYPE.PHOTO.name();
            }
        return FileMetadata.FILETYPE.valueOf(fileType).name();
    }
}
