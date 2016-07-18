/**
 *
 */
package com.ceitechs.domain.service.service;


import com.ceitechs.domain.service.domain.FileMetadata;
import com.ceitechs.domain.service.util.MetadataFields;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author iddymagohe
 * @since 1.0
 */
public interface GridFsService {

    /**
     * @param content
     * @param metadata
     * @param dbObject
     */
    void storeFiles(InputStream content, Map<String, String> metadata, Function<Map<String, String>, DBObject> dbObject);

    /**
     *
     * @param searchCriteria
     * @return
     */
    GridFSDBFile getProfilePicture(FileMetadata searchCriteria, ReferenceIdFor referenceIdFor);

    /**
     * Retrieves all attachments by {@link FileMetadata#referenceId}
     * @param searchCriteria
     * @param idFor
     * @return
     */
    List<GridFSDBFile> getAllAttachments(FileMetadata searchCriteria, ReferenceIdFor idFor);

    /**
     * Deletes a specific attachment by {@link FileMetadata#referenceId} and {@link FileMetadata#fileName}
     * @param fileName
     * @param referenceIdFor
     */
    void deleteAttachment(String fileName, String referenceId, ReferenceIdFor referenceIdFor);

    /**
     * Deletes all attachments by the {@link FileMetadata#referenceId} ,
     * optional siblings when {@link FileMetadata#parentReferenceId} is passed and @param deleteSiblings id true
     * @param referenceIdFor
     * @param deleteSiblings
     */
    void deleteAllAttachmentsFor(final FileMetadata searchCriteria, ReferenceIdFor referenceIdFor, boolean deleteSiblings);

}

@Service
class PangoGridFsServiceImpl implements GridFsService {

    @Autowired
    private GridFsOperations operations;

    private static final String METADATAFIELD = "metadata.";


    @Override
    public void storeFiles(final InputStream content, final Map<String, String> metadata, Function<Map<String, String>, DBObject> dbObject) {
        Assert.notNull(content, "file content can not be null");
        Assert.notNull(metadata, " Meta data can not be null");
        operations.store(content, metadata.get(MetadataFields.FILE_NAME), metadata.get(MetadataFields.CONTENT_TYPE), dbObject.apply(metadata));
    }

    @Override
    public GridFSDBFile getProfilePicture(FileMetadata searchCriteria, ReferenceIdFor referenceIdFor) {
        Assert.notNull(searchCriteria, "search criteria for thumbnail can not be null");
        Criteria criteria = new Criteria(getMetaFieldWrapper(MetadataFields.TYPE)).is((FileMetadata.FILETYPE.PHOTO.name()));
        criteria.and(getMetaFieldWrapper(referenceIdFor.getMetadataField())).is(searchCriteria.getReferenceId());
        criteria.and(getMetaFieldWrapper(MetadataFields.PROFILEPICTURE)).is("true");
        return operations.findOne(query(criteria));
    }


    @Override
    public List<GridFSDBFile> getAllAttachments(FileMetadata searchCriteria, ReferenceIdFor idFor) {
        Assert.notNull(searchCriteria, "Search criteria for attachments can not be null");
        Assert.hasText(searchCriteria.getFileType(),"File type can not be niull or empty");
        Criteria criteria = new Criteria(getMetaFieldWrapper(MetadataFields.TYPE)).is(FileMetadata.FILETYPE.valueOf(searchCriteria.getFileType().toUpperCase()).name());
        criteria.and(getMetaFieldWrapper(idFor.getMetadataField())).is(searchCriteria.getReferenceId());
        return operations.find(query(criteria));
    }

    @Override
    public void deleteAttachment(String fileName, String referenceId, ReferenceIdFor referenceIdFor) {
        Assert.hasText(fileName, "File name to delete can not be null or empty");
        Assert.hasText(referenceId, "Reference id can not be null or empty");
        Criteria criteria = new Criteria(MetadataFields.FILE_NAME).is(fileName);
        criteria.and(getMetaFieldWrapper(referenceIdFor.getMetadataField())).is(referenceId);
          operations.delete(query(criteria));
    }

    /**
     * Deletes all attachments by the {@link FileMetadata#referenceId} ,
     * optional siblings when {@link FileMetadata#parentReferenceId} is passed and @param deleteSiblings id true
     * @param referenceIdFor
     * @param deleteSiblings
     */
    @Override
    public void deleteAllAttachmentsFor(FileMetadata searchCriteria, ReferenceIdFor referenceIdFor, boolean deleteSiblings) {
        Assert.hasText(searchCriteria.getReferenceId(), "Reference id can not be null or empty");
        Criteria criteria = new Criteria();
        if(deleteSiblings && StringUtils.hasText(searchCriteria.getParentReferenceId())){
           referenceIdFor.getParentField().ifPresent( p -> criteria.and(getMetaFieldWrapper(p.getMetadataField())).is(searchCriteria.getReferenceId()) );
        }else{
            criteria.and(getMetaFieldWrapper(referenceIdFor.getMetadataField())).is(searchCriteria.getReferenceId());
        }
        operations.delete(query(criteria));
    }


    /**
     * meta-data fields are nested in {@value #METADATAFIELD}
     *
     * @param field
     * @return
     */
    static String getMetaFieldWrapper(String field) {
        return field.contains(METADATAFIELD) ? field : METADATAFIELD + field;
    }


}


