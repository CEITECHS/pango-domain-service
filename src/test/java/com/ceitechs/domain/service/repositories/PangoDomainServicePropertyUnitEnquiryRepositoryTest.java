package com.ceitechs.domain.service.repositories;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.CorrespondenceType;
import com.ceitechs.domain.service.domain.PropertyUnitEnquiry;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.util.PangoUtility;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.ceitechs.domain.service.repositories.PangoDomainServicePropertyUnitEnquiryRepository;

import static org.junit.Assert.*;

/**
 * @author iddymagohe
 * @since 1.0
 */
public class PangoDomainServicePropertyUnitEnquiryRepositoryTest extends AbstractPangoDomainServiceIntegrationTest {

    @Autowired
    PangoDomainServicePropertyUnitEnquiryRepository unitEnquiryRepository;
    @Autowired
    PangoDomainServiceUserRepository userRepository;

   @Test
   public void createAnEnquiryTest(){
       unitEnquiryRepository.deleteAll();
       assertTrue(unitEnquiryRepository.findAll().size() == 0);
       PropertyUnitEnquiry unitEnquiry = new PropertyUnitEnquiry();
       unitEnquiry.setEnquiryReferenceId(PangoUtility.generateIdAsString());
       unitEnquiry.setEnquiryType(CorrespondenceType.INTERESTED);
       unitEnquiry.setIntroduction("My name is so and so a teacher, looking for a family apartment");
       unitEnquiry.setMessage("I'm interested in renting property, was wondering whether the price is negotiable?");
       User usr = userRepository.findAll().get(0);
       assertNotNull("User must exist to create an Enquiry",usr);
       unitEnquiry.setProspectiveTenant(usr);
      // unitEnquiry.setPropertyUnit(null); //TODO : Make sure propeerty unit exists
       unitEnquiry.setSubject("RE: Your Property Listed on Pango Platform");
       PropertyUnitEnquiry savedEnq =  unitEnquiryRepository.save(unitEnquiry);
       assertNotNull("Saved Enquiry can not be null",savedEnq);
       assertTrue(unitEnquiryRepository.findAll().size() > 0);

    }
}
