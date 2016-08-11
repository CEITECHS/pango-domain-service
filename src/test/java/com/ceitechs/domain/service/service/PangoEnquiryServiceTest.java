package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.fail;

/**
 * @author  by iddymagohe on 8/10/16.
 */
public class PangoEnquiryServiceTest  extends AbstractPangoDomainServiceIntegrationTest{

    @Autowired PangoEnquiryService enquiryService;

    public void createUserEnquiryToPropertyTest(){
        //TODO included test - cases for exception scenarios
        fail("not implemented yet");
    }

    public void addEnquiryCorrespondenceTest(){
        //TODO test all exception scenarios
        // added correspondence
        // uploaded attachment
        fail("not yet implemented");
    }
}
