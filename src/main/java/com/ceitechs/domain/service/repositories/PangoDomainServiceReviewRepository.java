package com.ceitechs.domain.service.repositories;

import com.ceitechs.domain.service.domain.Review;
import com.sun.tools.javac.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author iddymagohe
 * @since 0.1
 */
public interface PangoDomainServiceReviewRepository extends MongoRepository<Review, String>{
    List<Review> findByPropertyUnity();
}
