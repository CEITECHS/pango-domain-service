package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.domain.EnquiryCorrespondence;
import com.ceitechs.domain.service.domain.PropertyUnit;
import com.ceitechs.domain.service.domain.PropertyUnitEnquiry;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.repositories.PropertyUnitEnquiryRepository;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import com.ceitechs.domain.service.util.PangoUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author  by iddymagohe on 8/9/16.
 */
public interface PangoEnquiryService {

    /**
     *
     * @param user
     * @param propertyReferenceId
     * @param enquiry
     * @return
     */
    Optional<PropertyUnitEnquiry> createUserEnquiryToProperty(User user, String propertyReferenceId, PropertyUnitEnquiry enquiry) throws EntityExists,EntityNotFound;

    /**
     *  adds a {@link EnquiryCorrespondence}  to an existing {@link PropertyUnitEnquiry}
     * @param user prospective tenant or property owner
     * @param enquiryReferenceId
     * @param correspondence
     * @throws EntityExists if there is 60days old open enquiry - otherwise.
     * @return
     */
    Optional<PropertyUnitEnquiry> addEnquiryCorrespondence(User user, String enquiryReferenceId, EnquiryCorrespondence correspondence);

    /**
     *  retrieves recent  enquiries made by the user to various properties
     * @param prospectiveTenant
     * @param count total enquiries to return
     * @return
     */
    List<PropertyUnitEnquiry> retrieveEnquiriesBy(User prospectiveTenant, int count);

    /**
     *  retrieve all enquiries made to a property optionalPropertyReferenceId when passed,
     *  Otherwise returns all enquiries made to properties owned by the by Owner.
     * @param owner
     * @param optionalPropertyReferenceId
     * @param count recent enquiries count to return per property in-case of optionalPropertyReferenceId is not passed
     * @return
     */
    List<PropertyUnitEnquiry> retrieveEnquiriesBy(User owner, Optional<String> optionalPropertyReferenceId, int count);

    /**
     *  retrieves all  enquires made by a user to property
     * @param user
     * @param propertyReferenceId
     * @return
     */
    List<PropertyUnitEnquiry> retrieveEnquiriesBy(User user, String propertyReferenceId);

}

@Service
class PangoEnquiryServiceImpl implements PangoEnquiryService {

    private static final Logger logger = LoggerFactory.getLogger(PangoEnquiryServiceImpl.class);


    private final UserRepository userRepository;


    private final PropertyUnitRepository propertyUnitRepository;


    private final PropertyUnitEnquiryRepository enquiryRepository;

    @Autowired
    public PangoEnquiryServiceImpl(UserRepository userRepository, PropertyUnitRepository propertyUnitRepository, PropertyUnitEnquiryRepository enquiryRepository) {
        this.userRepository = userRepository;
        this.propertyUnitRepository = propertyUnitRepository;
        this.enquiryRepository = enquiryRepository;
    }

    /**
     * @param user
     * @param propertyReferenceId
     * @param enquiry
     * @throws EntityNotFound for property or user
     * @throws EntityExists for an open 60 days old enquiry by the user to the property
     * @return
     */
    @Override
    public Optional<PropertyUnitEnquiry> createUserEnquiryToProperty(User user, String propertyReferenceId, PropertyUnitEnquiry enquiry) throws EntityExists, EntityNotFound {
        Assert.notNull(user, " for creating an enquiry user can not be null");
        Assert.hasText(user.getUserReferenceId(), "for creating an enquiry user reference id can not be null or empty");
        Assert.hasText(propertyReferenceId, "for creating an enquiry to a property: propertyReferenceId can not be null or empty");
        Assert.notNull(enquiry, "Enquiry can not be null");
        Assert.notNull(enquiry.getEnquiryType(), "Enquiry type can not be null or empty");
        Assert.notNull(enquiry.getMessage(), "Enquiry message can not be null or empty");

        // 1. check that a user exists and has verified their account
        User savedUser = userRepository.findByEmailAddressOrUserReferenceIdAllIgnoreCaseAndProfileVerifiedTrue("", user.getUserReferenceId());
        if (savedUser == null)
            throw new EntityNotFound(String.format("User : %s  does not exist or account has not been verified", user.getUserReferenceId()), new IllegalArgumentException(String.format("User : %s  does not exist or account has not been verified", user.getUserReferenceId())));

        //2. Check that a property ecists
        PropertyUnit propertyUnit = propertyUnitRepository.findOne(propertyReferenceId);
         if (propertyUnit == null )
             throw new EntityNotFound(String.format("Property : %s  does not exist", propertyReferenceId), new IllegalArgumentException(String.format("Property : %s  does not exist", propertyReferenceId)));

        //3. check that the user doesn't have an open enquiry to this property
        PropertyUnitEnquiry existingEnquiry = enquiryRepository.findByProspectiveTenantAndPropertyUnitOrderByEnquiryDateDesc(savedUser,propertyUnit);
        LocalDateTime sixtyDays = LocalDateTime.of(existingEnquiry.getEnquiryDate().toLocalDate(),existingEnquiry.getEnquiryDate().toLocalTime()).plusDays(60);
        if (existingEnquiry != null && existingEnquiry.getEnquiryDate().isBefore(sixtyDays))
            throw new EntityExists(String.format("There is an open Enquiry : %s for this user on this property : %s", existingEnquiry.getEnquiryReferenceId(), existingEnquiry.getPropertyUnit().getPropertyUnitId()), new IllegalStateException("there is an open Enquiry"));

        //4. Create a new enquiry to a property
        enquiry.setEnquiryReferenceId(PangoUtility.generateIdAsString());
        enquiry.setPropertyUnit(propertyUnit);
        enquiry.setProspectiveTenant(savedUser);
        PropertyUnitEnquiry savedEnquiry = enquiryRepository.save(enquiry);
        //TODO Trigger a push notification event to the owners
        return Optional.ofNullable(savedEnquiry);
    }

    /**
     * adds a {@link EnquiryCorrespondence}  to an existing {@link PropertyUnitEnquiry}
     *
     * @param user               prospective tenant or property owner
     * @param enquiryReferenceId
     * @param correspondence
     * @return
     */
    @Override
    public Optional<PropertyUnitEnquiry> addEnquiryCorrespondence(User user, String enquiryReferenceId, EnquiryCorrespondence correspondence) {
        return Optional.empty(); //TODO pending impl
    }

    /**
     * retrieves recent  enquiries made by the user to various properties
     *
     * @param prospectiveTenant
     * @param count             total enquiries to return
     * @return
     */
    @Override
    public List<PropertyUnitEnquiry> retrieveEnquiriesBy(User prospectiveTenant, int count) {
        return Collections.EMPTY_LIST; //TODO pending impl
    }

    /**
     * retrieve all enquiries made to a property optionalPropertyReferenceId when passed,
     * Otherwise returns all enquiries made to properties owned by the by Owner.
     *
     * @param owner
     * @param optionalPropertyReferenceId
     * @param count                       recent enquiries count to return per property in-case of optionalPropertyReferenceId is not passed
     * @return
     */
    @Override
    public List<PropertyUnitEnquiry> retrieveEnquiriesBy(User owner, Optional<String> optionalPropertyReferenceId, int count) {
        return Collections.EMPTY_LIST; //TODO pending impl
    }

    @Override
    public List<PropertyUnitEnquiry> retrieveEnquiriesBy(User user, String  propertyReferenceId) {
        return Collections.EMPTY_LIST; //TODO pending impl
    }
}


