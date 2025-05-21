package com.example.demo.service;

import com.example.demo.exception.ErrorAssignedException;
import com.example.demo.model.MachineError;
import com.example.demo.repository.MachineErrorRepository;
import com.example.demo.repository.MachineTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MachineErrorServiceTest {

    @Mock
    private MachineErrorRepository machineErrorRepository;

    @Mock
    private MachineTypeRepository machineTypeRepository;

    @InjectMocks
    private MachineErrorService machineErrorService;

    // No specific sampleError needed in BeforeEach as tests create their own specific instances.

    @BeforeEach
    void setUp() {
        // Mocks are automatically initialized by the @ExtendWith(MockitoExtension.class) annotation.
        // No explicit MockitoAnnotations.openMocks(this); needed.
    }

    // Tests for addError method
    @Test
    void testAddError_firstError_shouldSetUniqueIdToOne() {
        when(machineErrorRepository.findTopByOrderByUniqueIdDesc()).thenReturn(Optional.empty());
        MachineError newError = new MachineError(); // The error to be passed to the service
        // Mock save to return the error passed to it, which will have uniqueId set by the service
        when(machineErrorRepository.save(any(MachineError.class))).thenAnswer(invocation -> {
            MachineError errorToSave = invocation.getArgument(0);
            // Simulate setting an ID by the repository upon saving, if necessary for the test logic
            // errorToSave.setId("mockGeneratedId"); 
            return errorToSave;
        });

        MachineError savedError = machineErrorService.addError(newError);

        assertEquals(1, savedError.getUniqueId(), "UniqueId should be 1 for the first error.");
        verify(machineErrorRepository, times(1)).findTopByOrderByUniqueIdDesc();
        verify(machineErrorRepository, times(1)).save(newError); // Verify save was called with the newError object
    }

    @Test
    void testAddError_existingErrors_shouldSetUniqueIdToMaxPlusOne() {
        MachineError existingError = new MachineError();
        existingError.setUniqueId(5); // Max existing uniqueId
        when(machineErrorRepository.findTopByOrderByUniqueIdDesc()).thenReturn(Optional.of(existingError));
        
        MachineError newError = new MachineError(); // The error to be passed to the service
        // Mock save to return the error passed to it
        when(machineErrorRepository.save(any(MachineError.class))).thenAnswer(invocation -> {
            MachineError errorToSave = invocation.getArgument(0);
            return errorToSave;
        });

        MachineError savedError = machineErrorService.addError(newError);

        assertEquals(6, savedError.getUniqueId(), "UniqueId should be max existing uniqueId + 1.");
        verify(machineErrorRepository, times(1)).findTopByOrderByUniqueIdDesc();
        verify(machineErrorRepository, times(1)).save(newError);
    }

    // Tests for deleteError method
    @Test
    void testDeleteError_errorIsAssigned_shouldThrowErrorAssignedException() {
        MachineError errorToDelete = new MachineError();
        errorToDelete.setId("testId");
        errorToDelete.setUniqueId(100);

        when(machineErrorRepository.findById("testId")).thenReturn(Optional.of(errorToDelete));
        when(machineTypeRepository.existsByErrorsContains(100)).thenReturn(true);

        ErrorAssignedException thrownException = assertThrows(ErrorAssignedException.class, () -> {
            machineErrorService.deleteError("testId");
        }, "ErrorAssignedException should be thrown.");
        
        assertEquals("Error with uniqueId 100 is assigned to one or more machine types and cannot be deleted.", thrownException.getMessage());

        verify(machineErrorRepository, times(1)).findById("testId");
        verify(machineTypeRepository, times(1)).existsByErrorsContains(100);
        verify(machineErrorRepository, never()).deleteById("testId"); // Ensure deleteById was not called
    }

    @Test
    void testDeleteError_errorNotAssigned_shouldDeleteSuccessfully() {
        MachineError errorToDelete = new MachineError();
        errorToDelete.setId("testId");
        errorToDelete.setUniqueId(101);

        when(machineErrorRepository.findById("testId")).thenReturn(Optional.of(errorToDelete));
        when(machineTypeRepository.existsByErrorsContains(101)).thenReturn(false);
        // doNothing().when(machineErrorRepository).deleteById("testId"); // Not strictly necessary for void methods

        assertDoesNotThrow(() -> {
            machineErrorService.deleteError("testId");
        }, "Should not throw an exception when deleting an unassigned error.");

        verify(machineErrorRepository, times(1)).findById("testId");
        verify(machineTypeRepository, times(1)).existsByErrorsContains(101);
        verify(machineErrorRepository, times(1)).deleteById("testId"); // Verify deleteById was called
    }

    @Test
    void testDeleteError_errorNotFound_shouldThrowNoSuchElementException() {
        when(machineErrorRepository.findById("nonExistentId")).thenReturn(Optional.empty());

        NoSuchElementException thrownException = assertThrows(NoSuchElementException.class, () -> {
            machineErrorService.deleteError("nonExistentId");
        }, "NoSuchElementException should be thrown when error is not found.");

        assertEquals("MachineError not found with id: nonExistentId", thrownException.getMessage());

        verify(machineErrorRepository, times(1)).findById("nonExistentId");
        verify(machineTypeRepository, never()).existsByErrorsContains(anyInt()); // Ensure this is not called
        verify(machineErrorRepository, never()).deleteById(anyString()); // Ensure deleteById is not called
    }
}
