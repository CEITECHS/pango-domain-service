package com.ceitechs.domain.service.repositories;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.ceitechs.domain.service.AbstractPangoDomainServiceIntegrationTest;
import com.ceitechs.domain.service.domain.Review;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.util.PangoUtility;

/**
 * @author iddymagohe
 * @since 1.0
 */
public class ReviewRepositoryTest extends AbstractPangoDomainServiceIntegrationTest {

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    public void saveReviewByTenantTest() {
        reviewRepository.deleteAll();
        assertTrue(reviewRepository.findAll().size() == 0);
        Review review = new Review();
        review.setReviewReferenceId(PangoUtility.generateIdAsString());
        User usr = userRepository.findAll().get(0);
        assertNotNull(usr);
        review.setPropertyUnitReferenceId("12345gts");//TODO set propertyUnit Reference
        review.setReviewText("Honnestly speaking I like this Place Big times");
        review.setReviewedBy(String.format("%s, %s", usr.getFirstName(), usr.getLastName()));
        review.setRecommend(true);
        review.setRating(4.6);
        Review savedReview = reviewRepository.save(review);
        assertNotNull(savedReview);
    }

    @Test
    public void reviewsByPropertyTest() {
        reviewRepository.deleteAll();
        String randomStr = "12hfdhshhvfhjsdvft78y9y89";
        List<User> usrs = userRepository.findAll();
        assertTrue(usrs.size() > 0);
        IntStream.range(0, 10).forEach(i -> {
            User usr = i < usrs.size() ? usrs.get(i) : usrs.get(PangoUtility.random(0, usrs.size()-1));
            Review review = new Review();
            review.setReviewReferenceId(PangoUtility.generateIdAsString());
            review.setPropertyUnitReferenceId(randomStr);//TODO set propertyUnit Reference
            review.setReviewText("Honestly speaking I like this Place Big times XXX - "+ i);
            review.setReviewedBy(String.format("%s, %s", usr.getFirstName(), usr.getLastName()));
            review.setRecommend(true);
            review.setRating(PangoUtility.random(3.0,5.0));
            Review savedReview = reviewRepository.save(review);
        });

        Page<Review> reviewPage = reviewRepository.findByPropertyUnitReferenceIdOrderByCreatedDateDesc(randomStr, new PageRequest(0,50));
        //assertN(reviewPage.iterator());
        List<Review> reviews = PangoUtility.toArrayList(reviewPage.iterator());
        assertFalse(reviews.isEmpty());
        assertTrue(reviews.size() >= 10);
        reviewPage.forEach(System.out::println);
    }

    @Test
    public void saveReviewByLandlordTest() {
        reviewRepository.deleteAll();
        assertTrue(reviewRepository.findAll().size() == 0);
        Review review = new Review();
        review.setReviewReferenceId(PangoUtility.generateIdAsString());
        User usr = userRepository.findAll().get(0);
        assertNotNull(usr);
        review.setReviewText("I liked this Tenant for his entire stay");
        review.setTenantReferenceId(usr.getUserReferenceId());
        review.setReviewedBy(String.format("%s, %s", usr.getFirstName(), usr.getLastName())); ////TODO set property Owner's names Reference
        review.setRecommend(true);
        review.setRating(4.6);
        Review savedReview = reviewRepository.save(review);
        assertNotNull(savedReview);
    }

    @Test
    public void reviewsByTenantTest() {
        reviewRepository.deleteAll();
        User usr = userRepository.findAll().get(0);
        assertNotNull(usr);
        IntStream.range(0, 5).forEach(i -> {
            Review review = new Review();
            review.setReviewReferenceId(PangoUtility.generateIdAsString());
            review.setTenantReferenceId(usr.getUserReferenceId());
            review.setReviewText("I liked this Tenant for his entire stay Big times XXX - "+ i);
            review.setReviewedBy(String.format("%s, %s", usr.getFirstName(), usr.getLastName())); ////TODO set property Owner's names Reference
            review.setRecommend(true);
            review.setRating(PangoUtility.random(4.0,5.0));
            Review savedReview = reviewRepository.save(review);
        });

        Page<Review> reviewPage = reviewRepository.findByTenantReferenceIdOrderByCreatedDateDesc(usr.getUserReferenceId(), new PageRequest(0,50));
        //assertN(reviewPage.iterator());
        List<Review> reviews = PangoUtility.toArrayList(reviewPage.iterator());
        assertFalse(reviews.isEmpty());
        assertTrue(reviews.size() >= 4);
        reviewPage.forEach(System.out::println);
    }


}
