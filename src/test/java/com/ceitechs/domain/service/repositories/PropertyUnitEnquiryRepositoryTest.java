package com.ceitechs.domain.service.repositories;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.CorrespondenceType;
import com.ceitechs.domain.service.domain.EnquiryCorrespondence;
import com.ceitechs.domain.service.domain.PropertyUnitEnquiry;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.util.PangoUtility;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.*;

/**
 * @author iddymagohe
 * @since 1.0
 */
public class PropertyUnitEnquiryRepositoryTest extends AbstractPangoDomainServiceIntegrationTest {

    @Autowired
    PropertyUnitEnquiryRepository unitEnquiryRepository;
    @Autowired
    UserRepository userRepository;

   @Test
   public void createAnEnquiryTest(){
       unitEnquiryRepository.deleteAll();
       assertTrue(unitEnquiryRepository.findAll().size() == 0);
       PropertyUnitEnquiry unitEnquiry = newEnquiry();
       unitEnquiry.setEnquiryReferenceId(PangoUtility.generateIdAsString());

       User usr = userRepository.findAll().get(0);
       assertNotNull("User must exist to create an Enquiry",usr);
       unitEnquiry.setProspectiveTenant(usr);
      // unitEnquiry.setPropertyUnit(null); //TODO : Make sure propeerty unit exists
       PropertyUnitEnquiry savedEnq =  unitEnquiryRepository.save(unitEnquiry);
       assertNotNull("Saved Enquiry can not be null",savedEnq);
       assertTrue(unitEnquiryRepository.findAll().size() > 0);

    }

    @Test
    public void enquiriesByTenantTest(){
        unitEnquiryRepository.deleteAll();
        assertTrue(unitEnquiryRepository.findAll().size() == 0);
        PropertyUnitEnquiry unitEnquiry = newEnquiry();
        unitEnquiry.setEnquiryReferenceId(PangoUtility.generateIdAsString());
        User usr = userRepository.findAll().get(0);
        assertNotNull("User must exist to create an Enquiry",usr);
        unitEnquiry.setProspectiveTenant(usr);
        unitEnquiryRepository.save(unitEnquiry);
        Page<PropertyUnitEnquiry> propertyUnitEnquiryPage = unitEnquiryRepository.findByProspectiveTenantOrderByEnquiryDateDesc(usr, new PageRequest(0,50));
        assertThat("Returned Correspondence for this user should be 1 ", propertyUnitEnquiryPage.getContent(), hasSize(1));
    }

    @Test
    public void addCorrespondenceOnEnquiryTest(){
        unitEnquiryRepository.deleteAll();
        assertTrue(unitEnquiryRepository.findAll().size() == 0);
        PropertyUnitEnquiry unitEnquiry = newEnquiry();
        unitEnquiry.setEnquiryReferenceId(PangoUtility.generateIdAsString());
        User usr = userRepository.findAll().get(0);
        assertNotNull("User must exist to create an Enquiry",usr);
        unitEnquiry.setProspectiveTenant(usr);
        // unitEnquiry.setPropertyUnit(null); //TODO : Make sure property unit exists
        PropertyUnitEnquiry savedEnq =  unitEnquiryRepository.save(unitEnquiry);
        assertNotNull("Saved Enquiry can not be null",savedEnq);
        assertTrue(unitEnquiryRepository.updateEnquiryWith(unitEnquiry.getEnquiryReferenceId(),newCorrespondence()).isPresent());
        assertTrue(unitEnquiryRepository.updateEnquiryWith(unitEnquiry.getEnquiryReferenceId(),newCorrespondence()).isPresent());
        assertThat("The returned correspondence list shoud match the expected list", unitEnquiryRepository.findOne(unitEnquiry.getEnquiryReferenceId()).getCorrespondences(), hasSize(2));
    }

    static private PropertyUnitEnquiry newEnquiry(){
        PropertyUnitEnquiry unitEnquiry = new PropertyUnitEnquiry();
        unitEnquiry.setEnquiryType(CorrespondenceType.INTERESTED);
        unitEnquiry.setIntroduction("My name is so and so a teacher, looking for a family apartment");
        unitEnquiry.setMessage("I'm interested in renting property, was wondering whether the price is negotiable?");
        unitEnquiry.setSubject("RE: Your PropertyRemoved Listed on Pango Platform");
        return unitEnquiry;
    }

    static private EnquiryCorrespondence newCorrespondence(){
        EnquiryCorrespondence correspondence = new EnquiryCorrespondence();
        correspondence.setMessage("Thank you for your Message, and I'm glad that you're like this unit, it's great for your family size \n" +
                "I think you should plan to site visit and we'll take it from there \n" +
                "Whenever you get a chance please shoot me Your photo Id , possible site visit dates that works for you as i'm flexible");
        correspondence.setCorrespondenceReferenceId(System.currentTimeMillis());
        correspondence.setCorrespondenceType(CorrespondenceType.REQUEST_INFO);
        correspondence.setOwner(true);
        return correspondence;
    }


}
