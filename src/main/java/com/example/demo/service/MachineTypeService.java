package com.example.demo.service;

import com.example.demo.model.MachineType;
import com.example.demo.repository.MachineErrorRepository;
import com.example.demo.repository.MachineTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MachineTypeService {

    @Autowired
    private MachineTypeRepository machineTypeRepository;

    @Autowired
    private MachineErrorRepository machineErrorRepository; // Not used in the current methods, but autowired as per requirements

    public List<MachineType> getAllMachineTypes() {
        return machineTypeRepository.findAll();
    }

    public Optional<MachineType> getMachineTypeByName(String name) {
        return machineTypeRepository.findByName(name);
    }

    public MachineType saveMachineType(MachineType machineType) {
        // This method can be used for both creating and updating.
        // For example, when adding/removing errors, the list of error uniqueIds in MachineType would be updated
        // and then this method would be called to save the changes.
        return machineTypeRepository.save(machineType);
    }
}
