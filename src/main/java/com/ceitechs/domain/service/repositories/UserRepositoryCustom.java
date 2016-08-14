package com.ceitechs.domain.service.repositories;


import com.ceitechs.domain.service.domain.Annotations.Updatable;
import com.ceitechs.domain.service.domain.PropertyUnit;
import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.domain.UserPreference;
import com.ceitechs.domain.service.util.PangoUtility;
import com.mongodb.BasicDBObject;
import com.mysema.commons.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author iddymagohe
 * @since 1.0 -
 */
public interface UserRepositoryCustom {

    /**
     *
     * @param preference
     * @param user
     * @return
     */
    Optional<User> addUserPreferences(UserPreference preference, User user);

    /**
     *  retrieve user-set preferences
     * @param userId
     * @return
     */
    List<UserPreference> retrievePreferencesBy(String userId);

    /**
     *
     * @param userPreference
     * @param user
     */
    Optional<User> updateUserPreference(UserPreference userPreference, User user);

    Optional<User> removePreferenceBy(String preferenceId, User user);

    Optional<User> updateFavouredProperties(User user, PropertyUnit propertyUnit, boolean favourable);
}

@Service
 class UserRepositoryImpl implements  UserRepositoryCustom{

    @Autowired
    UserRepository userRepository;

    @Autowired
    MongoOperations mongoOperations;

    @Override
    public Optional<User> addUserPreferences(UserPreference preference, User user) {
        Assert.notNull(user, "User can not be null");
        Assert.notNull(preference, "preference can not be null");
        Assert.hasText(user.getUserReferenceId(),"User-Id can not be null");
        if(!StringUtils.hasText(preference.getPreferenceId()))
             preference.setPreferenceId(PangoUtility.generateIdAsString());
        Update update = new Update().push("preferences",  preference);
        User usr = mongoOperations.findAndModify(query(Criteria.where("_id").is(user.getUserReferenceId())), update, FindAndModifyOptions.options().returnNew(true), User.class);
        return Optional.ofNullable(usr);

    }

    @Override
    public List<UserPreference> retrievePreferencesBy(String userId) {
        Assert.hasText(userId, "user Id can not be null or Empty");
        Criteria criteria = Criteria.where("_id").is(userId).andOperator(Criteria.where("profile.verified").is(true));
        Query query = query(criteria);
        query.fields().include("preferences");
        User user = mongoOperations.findOne(query, User.class);
        return user.getPreferences().isEmpty() ? user.getPreferences() :
                user.getPreferences().stream().filter(userPreference -> userPreference.getCategory() != UserPreference.PreferenceCategory.SEARCH || userPreference.getPreferenceType() != UserPreference.PreferenceType.NotforDisplay)
                        .sorted(Comparator.comparing(UserPreference::isActive).thenComparing(UserPreference::getCreatedOn).reversed())
                        .limit(10)
                        .collect(Collectors.toList());
    }

    /**
     * @param userPreference
     * @param user
     */
    @Override
    public Optional<User> updateUserPreference(UserPreference userPreference, User user) {
        Assert.notNull(userPreference, "user preference to update can not be null");
        Assert.notNull(user, "user  to update preference on can not be null");
        Assert.hasText(user.getUserReferenceId(),"User referenceId can not be empty or null for updating preference");
        Assert.hasText(userPreference.getPreferenceId(), "preference Identifier to update can not be null or empty");

       removePreferenceBy(userPreference.getPreferenceId(),user);
    return addUserPreferences(userPreference,user);
    }

    @Override
    public Optional<User> removePreferenceBy(String preferenceId, User user) {
        Assert.hasText(preferenceId, "preference Id can not be null or empty");
        Assert.notNull(user, "User can not be null");
        Assert.hasText(user.getUserReferenceId(), "user Id can not be null or empty");
        Update update = new Update().pull("preferences",new BasicDBObject("preferenceId",preferenceId));
        mongoOperations.updateFirst(query(Criteria.where("_id").is(user.getUserReferenceId()).and("preferences").elemMatch(Criteria.where("preferenceId").is(preferenceId))),update, User.class);
        return Optional.ofNullable(user);
    }

    @Override
    public Optional<User> updateFavouredProperties(User user, PropertyUnit propertyUnit, boolean favourable) {
        Assert.notNull(user, "user can not be null");
        Assert.notNull(propertyUnit, "property can not be null");
        Assert.hasText(propertyUnit.getPropertyUnitId(),"property reference id can not be null or empty");
        Update update = favourable ? new Update().addToSet("favouredProperties",propertyUnit.getPropertyUnitId()) : new Update().pull("favouredProperties", propertyUnit.getPropertyUnitId());
         mongoOperations.updateFirst(query(Criteria.where("_id").is(user.getUserReferenceId())), update, User.class);
        return Optional.of(user);
    }
}
