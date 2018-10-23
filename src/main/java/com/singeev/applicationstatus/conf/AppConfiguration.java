//package com.singeev.applicationstatus.conf;
//
//import com.mongodb.MongoClient;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
//import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
//
//@Configuration
//@EnableMongoRepositories(basePackages = "com.singeev.applicationstatus")
//public class AppConfiguration {
//
//    public String getDatabaseName() {
//        return "visaStatus";
//    }
//
//    public MongoClient mongoClient() {
//        return new MongoClient("localhost", 27017);
//    }
//
//    @Bean
//    public MongoTemplate mongoTemplate(){
//        return new MongoTemplate(
//                new SimpleMongoDbFactory(
//                        mongoClient(),
//                        getDatabaseName()
//                )
//        );
//    }
//}
