/**
 * 
 */
package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.Attachment;
import com.ceitechs.domain.service.domain.FileMetadata;
import com.ceitechs.domain.service.util.MetadataFields;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import sun.jvm.hotspot.oops.MetadataField;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author iddymagohe
 * @since 0.1
 */
public class GridFsServiceTest extends AbstractPangoDomainServiceIntegrationTest {
	
	@Autowired
	private GridFsService gridFsService;

	@Autowired
	private GridFsOperations operations;
	
	@Test
	public void storeFilesTest() throws IOException{
		operations.delete(null);
        assertTrue(operations.find(null).size() == 0);
		Map<String,String> metadataMap=  PangoUtility.attachmentMetadataToMap("1", ReferenceIdFor.PROPERTY,"",buildAttachment());
        gridFsService.storeFiles(resource.getInputStream(), metadataMap, BasicDBObject::new);
		 // then
		 List<GridFSDBFile> files = operations.find(null);
		 assertTrue(files.size() > 0);

        gridFsService.deleteAttachment(resource.getFilename(),"1", ReferenceIdFor.PROPERTY);
        List<GridFSDBFile> filesfew = operations.find(null);
        assertTrue(files.size() > filesfew.size() );
	}
	
	@Test
	public void getProfilePictureTest() throws IOException {
        operations.delete(null);
        assertTrue(operations.find(null).size() == 0);
       Attachment attachment = buildAttachment();
        Map<String,String> metadataMap=  PangoUtility.attachmentMetadataToMap("1", ReferenceIdFor.PROPERTY,"",attachment);
        gridFsService.storeFiles(resource.getInputStream(), metadataMap, BasicDBObject::new);
        FileMetadata meta = new FileMetadata();
        meta.setReferenceId("1");
		GridFSDBFile file = gridFsService.getProfilePicture(meta,ReferenceIdFor.PROPERTY);
		assertNotNull(file);
        assertEquals(meta.getReferenceId(), FileMetadata.getFileMetadataFromGridFSDBFile(Optional.of(file), ReferenceIdFor.PROPERTY).getReferenceId());
        gridFsService.deleteAttachment(resource.getFilename(),"1", ReferenceIdFor.PROPERTY);
        assertTrue(operations.find(null).size() == 0);


	}
	

	@Test
	public void getAllAttachmentTest() throws IOException, CloneNotSupportedException {
        operations.delete(null);
        assertTrue(operations.find(null).size() == 0);
        Attachment at1 = buildAttachment();
        Attachment at2 = at1.clone();
        at2.setProfilePicture(false);
        at2.setFileName("second-"+resource.getFilename());
        Attachment at3 = at1.clone();
        at3.setProfilePicture(false);
        at3.setFileName("third-"+resource.getFilename());
        Attachment at4 = at1.clone();
        Stream.of(at1,at2,at3).forEach(at ->{
            try {
                Map<String,String> metadataMap=  PangoUtility.attachmentMetadataToMap("10", ReferenceIdFor.UNIT_PROPERTY,"1",at);
                gridFsService.storeFiles(resource.getInputStream(), metadataMap, BasicDBObject::new);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        assertTrue(operations.find(null).size() >= 3);
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setReferenceId("10");
        fileMetadata.setFileType(FileMetadata.FILETYPE.PHOTO.name());
        List<GridFSDBFile> files = gridFsService.getAllAttachments(fileMetadata, ReferenceIdFor.UNIT_PROPERTY);
        assertNotNull(files);
        assertTrue(files.size() >= 3);
        List<FileMetadata> metadatas = files.stream()
                .map(gr -> FileMetadata.getFileMetadataFromGridFSDBFile(Optional.of(gr), ReferenceIdFor.UNIT_PROPERTY))
                .collect(toList());
        metadatas.forEach(System.out::println);

       gridFsService.deleteAllAttachmentsFor(fileMetadata,ReferenceIdFor.UNIT_PROPERTY,false);
        assertTrue(operations.find(null).size() < files.size());
    }
	
	private static Attachment buildAttachment() throws IOException {
		Attachment attachment = new Attachment();
		attachment.setFileType(FileMetadata.FILETYPE.PHOTO.name());
		attachment.setFileName(resource.getFilename());
		attachment.setFileSize(resource.getFile().length());
		attachment.setFileDescription("profile_picture");
        attachment.setProfilePicture(true);
		return attachment;
	}

}
