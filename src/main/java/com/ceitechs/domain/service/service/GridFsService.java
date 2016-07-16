/**
 *
 */
package com.ceitechs.domain.service.service;


import com.ceitechs.domain.service.domain.FileMetadata;
import com.ceitechs.domain.service.util.MetadataFields;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
    public void storeFiles(InputStream content, Map<String, String> metadata, Function<Map<String, String>, DBObject> dbObject);

    public GridFSDBFile getUserProfilePicture(FileMetadata searchCriteria);

    public List<GridFSDBFile> getAllAttachments(FileMetadata searchCriteria, ReferenceIdFor idFor);

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
    public GridFSDBFile getUserProfilePicture(final FileMetadata searchCriteria) {
        Assert.notNull(searchCriteria, "search criteria for thumbnail can not be null");
        Criteria criteria = new Criteria(getMetaFieldWrapper(MetadataFields.TYPE)).is((FileMetadata.FILETYPE.PHOTO.name()));
        criteria.and(getMetaFieldWrapper(MetadataFields.USER_REFERENCE_ID)).is(searchCriteria.getReferenceId());
        criteria.and(getMetaFieldWrapper(MetadataFields.PROFILEPICTURE)).is("true");
        return operations.findOne(query(criteria));
    }


    @Override
    public List<GridFSDBFile> getAllAttachments(FileMetadata searchCriteria, ReferenceIdFor idFor) {
        Assert.notNull(searchCriteria, "Search criteria for photo gallery can not be null");
        Criteria criteria = new Criteria(getMetaFieldWrapper(MetadataFields.TYPE)).is(FileMetadata.FILETYPE.PHOTO.name());
        criteria.and(getMetaFieldWrapper(idFor.getMetadataField())).is(searchCriteria.getReferenceId());
        return operations.find(query(criteria));
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


