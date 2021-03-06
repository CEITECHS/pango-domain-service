package com.ceitechs.domain.service.domain;

import com.ceitechs.domain.service.util.MetadataFields;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.gridfs.GridFSDBFile;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Ref;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author iddymagohe
 * @since 0.1
 */
@Getter
@Setter
public class FileMetadata {

    private static final Logger logger = LoggerFactory.getLogger(FileMetadata.class);

    public enum FILETYPE {
        PHOTO("image/"),
        DOCUMENT("image/"),
        VIDEO("application/");

        @Getter
        private String suffix;

        FILETYPE(String suffix) {
            this.suffix = suffix;
        }
    }

    private String referenceId;
    private String parentReferenceId;
    private String grandParentReferenceId; // applicable to correspondence attachment.
    private String fileType; //PHOTO or VIDEO,Document
    private boolean thumbnail; // applicable to properties
    private String fileName;
    private String contentType;
    private String contentBase64;
    private String caption;


    /**
     * Converts a #GridFSDBFile to  {@link FileMetadata} for Transfer.
     *
     * @param fileData
     * @param referenceIdFor
     */
    public static FileMetadata getFileMetadataFromGridFSDBFile(Optional<GridFSDBFile> fileData, ReferenceIdFor referenceIdFor) {
        FileMetadata fileMetadata = new FileMetadata();
        fileData.ifPresent(file -> {
            fileMetadata.setContentType(file.getContentType());
            fileMetadata.setFileName(file.getFilename());
            FileMetadata.fieldAsStringFromGridFSDBFile(file, MetadataFields.TYPE).ifPresent(fileMetadata::setFileType);
            FileMetadata.fieldAsStringFromGridFSDBFile(file, MetadataFields.PROFILEPICTURE).ifPresent(value -> fileMetadata.setThumbnail(Boolean.valueOf(value)));
            FileMetadata.fieldAsStringFromGridFSDBFile(file, referenceIdFor.getMetadataField()).ifPresent(fileMetadata::setReferenceId);
            referenceIdFor.getParentField().ifPresent(parentField -> {
                FileMetadata.fieldAsStringFromGridFSDBFile(file, parentField.getMetadataField()).ifPresent(fileMetadata::setParentReferenceId);
                   parentField.getParentField().ifPresent(grandParentField ->
                        FileMetadata.fieldAsStringFromGridFSDBFile(file, grandParentField.getMetadataField()).ifPresent(fileMetadata::setGrandParentReferenceId));
            });
            FileMetadata.fieldAsStringFromGridFSDBFile(file, MetadataFields.FILE_DESCR).ifPresent(fileMetadata::setCaption);
            try {
                PangoUtility.InputStreamToBase64(Optional.ofNullable(file.getInputStream()), file.getContentType()).ifPresent(fileMetadata::setContentBase64);
            } catch (Exception e) {
                 logger.error(e.getMessage() , e.getCause());
            }
        });
        return  fileMetadata;
    }

    public static Map<String,FileMetadata> getFileMetaFromGridFSDBFileAsMap(List<GridFSDBFile> gridFSDBFiles){
        if (gridFSDBFiles !=null && !gridFSDBFiles.isEmpty()){
            return gridFSDBFiles.parallelStream().map(gridFSDBFile -> getFileMetadataFromGridFSDBFile(Optional.of(gridFSDBFile), ReferenceIdFor.PROPERTY))
                    .collect(Collectors.toMap(FileMetadata::getReferenceId,fileMetada -> fileMetada));
        }
        return Collections.emptyMap();
    }

    public static List<FileMetadata> getFileMetaFromGridFSDBFileAsList(List<GridFSDBFile> gridFSDBFiles){
        if (gridFSDBFiles !=null && !gridFSDBFiles.isEmpty()){
            return gridFSDBFiles.parallelStream().map(gridFSDBFile -> getFileMetadataFromGridFSDBFile(Optional.of(gridFSDBFile), ReferenceIdFor.PROPERTY))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public static Optional<String> fieldAsStringFromGridFSDBFile(final GridFSDBFile file, final String field) {
        return Optional.ofNullable(String.valueOf(file.getMetaData().get(field)));
    }

    public static Optional<String> fieldAsStringFromMap(final Map<String, String> map, final String field) {
        return Optional.ofNullable(map.get(field));
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "referenceId='" + referenceId + '\'' +
                ", parentReferenceId='" + parentReferenceId + '\'' +
                ", grandParentReferenceId='" + grandParentReferenceId + '\'' +
                ", fileType='" + fileType + '\'' +
                ", thumbnail=" + thumbnail +
                ", fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", caption='" + caption + '\'' +
                '}';
    }
}
