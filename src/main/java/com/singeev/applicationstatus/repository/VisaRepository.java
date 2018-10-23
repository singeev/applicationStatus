package com.singeev.applicationstatus.repository;

import com.singeev.applicationstatus.dto.Visa;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisaRepository extends MongoRepository<Visa, String> {
}
