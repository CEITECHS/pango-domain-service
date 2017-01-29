package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.domain.Attachment;
import org.springframework.stereotype.Service;

/**
 * @author iddymagohe on 1/28/17.
 */
public interface FileStorageService {

    Attachment storeFile(Attachment attachment) throws Exception;

    String resolveUrl(Attachment attachment) throws Exception;

    void removeFile(Attachment attachment) throws Exception;
}

@Service
class AWSS3FileStorageService implements FileStorageService {

    @Override
    public Attachment storeFile(Attachment attachment) throws Exception {
        //TODO pending S3 impl
      return attachment;
    }

    @Override
    public String resolveUrl(Attachment attachment) throws Exception {
        //TODO pending impl
        return attachment.getBucket() + "/" + attachment.getReferenceId();
    }

    @Override
    public void removeFile(Attachment attachment) throws Exception {
        //TODO pending impl
        return;
    }


}
