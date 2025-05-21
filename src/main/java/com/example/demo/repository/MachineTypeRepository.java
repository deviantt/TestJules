package com.example.demo.repository;

import com.example.demo.model.MachineType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MachineTypeRepository extends MongoRepository<MachineType, String> {

    Optional<MachineType> findByName(String name);
}
