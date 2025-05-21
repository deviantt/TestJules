package com.example.demo.repository;

import com.example.demo.model.MachineError;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MachineErrorRepository extends MongoRepository<MachineError, String> {

    Optional<MachineError> findByUniqueId(Integer uniqueId);
}
