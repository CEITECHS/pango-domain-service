/**
 * 
 */
package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.Attachment;
import com.ceitechs.domain.service.domain.FileMetadata;
import com.ceitechs.domain.service.util.PangoUtility;
import com.ceitechs.domain.service.util.ReferenceIdFor;
import com.mongodb.BasicDBObject;
import com.mongodb.gridfs.GridFSDBFile;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
	
//	@Test
//	public void getAllProviderPhotosTest(){
//		FileMetadata meta = new FileMetadata("iddy", FileMetadata.FILETYPE.PHOTO.name(), null);
//		List<GridFSDBFile> files = gridFsService.getAllProviderPhotos(meta);
//		assertNotNull(files);
//		System.out.println(files.size());
//		assertTrue(files.size() > 0);
//		assertTrue(files.get(0).getMetaData().get("provider_id").equals(meta.getProviderId()));
//	}
	
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
