package com.ceitechs.domain.service.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

/**
 * 
 * @author abhisheksingh -
 * @since 1.0
 */
@Configuration
@EnableMongoRepositories("com.ceitechs.domain.service.repositories")
public class PangoDomainServiceMongoConfiguration extends AbstractMongoConfiguration {

    private final static String HOSTS_SEPARATOR = ",";
    private final static String HOST_PORT_SEPARATOR = ":";

    @Value("${db.password}")
    private String dbPassword;

    @Value("${db.user}")
    private String dbuser;

    @Value("${db.host.name}")
    private String host;

    @Value("${db.name}")
    private String dbName;

    @Override
    protected String getDatabaseName() {
        return dbName;
    }

    @Override
    public Mongo mongo() throws Exception {
        List<ServerAddress> addresses = Stream.of(host.split(HOSTS_SEPARATOR))
                .map(addr -> {
                    String[] hostAndport = addr.split(HOST_PORT_SEPARATOR);
                        return new ServerAddress(hostAndport[0], Integer.valueOf(hostAndport[1]));
                }).collect(Collectors.toList());

        MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(dbuser, dbName,
                dbPassword.toCharArray());
        Mongo mongo = new MongoClient(addresses, Arrays.asList(mongoCredential));
        mongo.setWriteConcern(WriteConcern.MAJORITY);
        return mongo;
    }
}
