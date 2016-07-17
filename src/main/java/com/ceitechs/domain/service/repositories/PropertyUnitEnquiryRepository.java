package com.ceitechs.domain.service.repositories;

import com.ceitechs.domain.service.domain.PropertyUnit;
import com.ceitechs.domain.service.domain.PropertyUnitEnquiry;
import com.ceitechs.domain.service.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author iddymagohe
 * @since 0.1
 */
public interface PropertyUnitEnquiryRepository extends MongoRepository<PropertyUnitEnquiry, String>,PropertyUnitEnquiryRepositoryCustom {

    Page <PropertyUnitEnquiry> findByPropertyUnitOrderByEnquiryDateDesc(PropertyUnit propertyUnit, Pageable page);
    Page <PropertyUnitEnquiry> findByProspectiveTenantOrderByEnquiryDateDesc(User prospectiveTenant,Pageable page);
}
