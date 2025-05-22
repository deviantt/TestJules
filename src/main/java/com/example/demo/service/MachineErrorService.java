package com.example.demo.service;

import com.example.demo.exception.ErrorAssignedException;
import com.example.demo.model.MachineError;
import com.example.demo.repository.MachineErrorRepository;
import com.example.demo.repository.MachineTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class MachineErrorService {

    @Autowired
    private MachineErrorRepository machineErrorRepository;

    @Autowired
    private MachineTypeRepository machineTypeRepository;

    public List<MachineError> getAllErrors() {
        return machineErrorRepository.findAll();
    }

    public MachineError addError(MachineError error) {
        Optional<MachineError> highestError = machineErrorRepository.findTopByOrderByUniqueIdDesc();
        if (highestError.isPresent()) {
            error.setUniqueId(highestError.get().getUniqueId() + 1);
        } else {
            error.setUniqueId(1);
        }
        return machineErrorRepository.save(error);
    }

    public void deleteError(String id) {
        MachineError errorToDelete = machineErrorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("MachineError not found with id: " + id));

        Integer uniqueId = errorToDelete.getUniqueId();
        if (machineTypeRepository.existsByErrorsContains(uniqueId)) {
            throw new ErrorAssignedException("Error with uniqueId " + uniqueId + " is assigned to one or more machine types and cannot be deleted.");
        }

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
