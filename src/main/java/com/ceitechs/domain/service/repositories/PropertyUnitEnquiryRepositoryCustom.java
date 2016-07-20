package com.ceitechs.domain.service.repositories;

import com.ceitechs.domain.service.domain.EnquiryCorrespondence;
import com.ceitechs.domain.service.domain.PropertyUnitEnquiry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;

import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author iddymagohe
 * @since 0.1
 */

public interface PropertyUnitEnquiryRepositoryCustom {
    /**
     *
     * @param enquiryId
     * @param correspondence
     * @return
     */
    Optional<PropertyUnitEnquiry>  updateEnquiryWith(String enquiryId, EnquiryCorrespondence correspondence);
}

class PropertyUnitEnquiryRepositoryImpl implements PropertyUnitEnquiryRepositoryCustom {

    @Autowired
    MongoOperations mongoOperations;

    @Autowired
    PropertyUnitEnquiryRepository enquiryRepository;

    @Override
    public Optional<PropertyUnitEnquiry> updateEnquiryWith(String enquiryId, EnquiryCorrespondence correspondence) {
        Assert.hasText(enquiryId, "EnquiryId can not be null or Empty");
        if (enquiryRepository.exists(enquiryId)){
            Update update = new Update().push("correspondences", correspondence); //TODO bring in QueryDSL
            PropertyUnitEnquiry propertyUnitEnquiry = mongoOperations.findAndModify(query(Criteria.where("_id").is(enquiryId)),update, PropertyUnitEnquiry.class);
           return   Optional.of(propertyUnitEnquiry);
        }
        return Optional.empty();
    }
}