package com.ceitechs.domain.service.repositories;


import com.ceitechs.domain.service.domain.User;
import com.ceitechs.domain.service.domain.UserPreference;
import com.ceitechs.domain.service.util.PangoUtility;
import com.mysema.commons.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * @author iddymagohe
 * @since 1.0
 */
public interface UserRepositoryCustom {

    Optional<User> addUserPreferences(UserPreference preference, User user);
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
        User usr = mongoOperations.findAndModify(query(Criteria.where("_id").is(user.getUserReferenceId())), update, User.class);
        return Optional.of(usr);

    }
}
