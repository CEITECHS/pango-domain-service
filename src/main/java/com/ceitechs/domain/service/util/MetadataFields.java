/**
 * 
 */
package com.ceitechs.domain.service.util;

/**
 * @author iddymagohe
 * @since 1.0
 */
public interface MetadataFields {
	String USER_REFERENCE_ID = "provider_id";
	String PROPERTY_REFERENCE_ID="";
	String UNIT_REFERENCE_ID="";
	String TYPE = "type";
	String CONTENT_TYPE = "contentType";
	String THUMBNAIL = "thumbnail";
	String FILE_NAME="filename";
	String FILE_DESCR="description";
	String REQURED_META="A minimum of filename and ContentType metadata is required";
}
