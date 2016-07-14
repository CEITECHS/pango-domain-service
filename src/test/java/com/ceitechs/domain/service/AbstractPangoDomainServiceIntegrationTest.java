package com.ceitechs.domain.service;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ceitechs.domain.service.config.PangoDomainServiceConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {"db.host.name = localhost:27017", "db.password = pangoPreprodWriteUsrPass10",
        "db.user =pangoWriteUser", "db.name = pango"})
@ContextConfiguration(classes = {PangoDomainServiceConfig.class})
public class AbstractPangoDomainServiceIntegrationTest {

}
