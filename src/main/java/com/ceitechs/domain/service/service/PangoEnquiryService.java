package com.ceitechs.domain.service.service;

import com.ceitechs.domain.service.domain.EnquiryCorrespondence;
import com.ceitechs.domain.service.domain.PropertyUnitEnquiry;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.repositories.PropertyUnitEnquiryRepository;
import com.ceitechs.domain.service.repositories.PropertyUnitRepository;
import com.ceitechs.domain.service.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
    Optional<PropertyUnitEnquiry> createUserEnquiryToProperty(User user, String propertyReferenceId, PropertyUnitEnquiry enquiry) throws EntityExists;

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
     * @return
     */
    @Override
    public Optional<PropertyUnitEnquiry> createUserEnquiryToProperty(User user, String propertyReferenceId, PropertyUnitEnquiry enquiry) throws EntityExists {
        Assert.notNull(user, " for creating an enquiry user can not be null");
        Assert.hasText(propertyReferenceId, "for creating an enquiry to a property: propertyReferenceId can not be null or empty");
        Assert.notNull(enquiry, "Enquiry can not be null");
        return Optional.empty(); //TODO pending impl
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


