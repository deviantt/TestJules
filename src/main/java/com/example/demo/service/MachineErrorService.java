package com.example.demo.service;

import com.example.demo.model.MachineError;
import com.example.demo.repository.MachineErrorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MachineErrorService {

    @Autowired
    private MachineErrorRepository machineErrorRepository;

    public List<MachineError> getAllErrors() {
        return machineErrorRepository.findAll();
    }

    public MachineError addError(MachineError error) {
        // Simple save for now, without checking for existing uniqueId
        return machineErrorRepository.save(error);
    }

    public void deleteError(String id) {
        machineErrorRepository.deleteById(id);
    }

    public MachineError updateError(String id, MachineError errorDetails) {
        return machineErrorRepository.findById(id).map(error -> {
            error.setDescription(errorDetails.getDescription());
            error.setLevel(errorDetails.getLevel());
            error.setType(errorDetails.getType());
            error.setSystem(errorDetails.getSystem());
            // uniqueId, spn, and fmi are not updated
            return machineErrorRepository.save(error);
        }).orElse(null); // Or throw an exception if not found
    }

    public Optional<MachineError> getErrorByUniqueId(Integer uniqueId) {
        return machineErrorRepository.findByUniqueId(uniqueId);
    }

    public Optional<MachineError> getErrorById(String id) {
        return machineErrorRepository.findById(id);
    }
}
