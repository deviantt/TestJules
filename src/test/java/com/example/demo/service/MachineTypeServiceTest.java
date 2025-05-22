package com.example.demo.service;

import com.example.demo.model.MachineType;
import com.example.demo.repository.MachineErrorRepository;
import com.example.demo.repository.MachineTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MachineTypeServiceTest {

    @Mock
    private MachineTypeRepository machineTypeRepository;

    @Mock
    private MachineErrorRepository machineErrorRepository; // Mocked as it's a dependency

    @InjectMocks
    private MachineTypeService machineTypeService;

    // Test objects can be initialized in BeforeEach or within each test method
    // For clarity, specific objects for each test will be created within the test methods.

    @BeforeEach
    void setUp() {
        // Mocks are automatically initialized by @ExtendWith(MockitoExtension.class)
        // No explicit MockitoAnnotations.openMocks(this); is needed.
    }

    @Test
    void testSaveMachineType_shouldSaveAndReturnMachineType() {
        // Arrange
        List<Integer> errorIds = Arrays.asList(1, 2, 3);
        MachineType machineTypeToSave = new MachineType();
        machineTypeToSave.setName("Test Type");
        machineTypeToSave.setFirmware("v1.0");
        machineTypeToSave.setErrors(errorIds);

        when(machineTypeRepository.save(any(MachineType.class))).thenAnswer(invocation -> {
            MachineType savedType = invocation.getArgument(0);
            // Simulate repository assigning an ID if that's relevant for the object's state after save
            // savedType.setId("mockRepoId"); 
            return savedType;
        });

        // Act
        MachineType savedMachineType = machineTypeService.saveMachineType(machineTypeToSave);

        // Assert
        assertNotNull(savedMachineType, "Saved machine type should not be null.");
        assertEquals("Test Type", savedMachineType.getName(), "Name should match.");
        assertEquals("v1.0", savedMachineType.getFirmware(), "Firmware should match.");
        assertEquals(errorIds, savedMachineType.getErrors(), "Error list should match the original list.");
        assertEquals(3, savedMachineType.getErrors().size(), "Error list size should be 3.");
        assertTrue(savedMachineType.getErrors().containsAll(Arrays.asList(1, 2, 3)), "Error list should contain all specified error IDs.");

        verify(machineTypeRepository, times(1)).save(machineTypeToSave); // Verify save was called with the machineTypeToSave object
    }

    @Test
    void testSaveMachineType_whenErrorsListIsModified_shouldReflectChanges() {
        // Arrange
        // This test simulates updating a machine type, possibly with a modified list of errors.
        // The service's saveMachineType is straightforward; it just calls repository.save().
        // The critical part is that the `modifiedMachineType` object passed to the service
        // already has the intended state (e.g., errors list updated by the controller).
        
        List<Integer> initialErrorIds = Arrays.asList(10, 20, 30);
        // MachineType originalMachineType = new MachineType(); // Not strictly needed for this test's logic
        // originalMachineType.setName("Update Type");
        // originalMachineType.setFirmware("v2.0");
        // originalMachineType.setErrors(initialErrorIds);

        List<Integer> modifiedErrorIds = Arrays.asList(10, 30); // Error 20 is removed
        MachineType modifiedMachineType = new MachineType();
        modifiedMachineType.setName("Update Type");
        modifiedMachineType.setFirmware("v2.0");
        modifiedMachineType.setErrors(modifiedErrorIds);

        // Mock repository to return the modifiedMachineType when save is called
        when(machineTypeRepository.save(any(MachineType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MachineType resultMachineType = machineTypeService.saveMachineType(modifiedMachineType);

        // Assert
        assertNotNull(resultMachineType, "Resulting machine type should not be null.");
        assertEquals("Update Type", resultMachineType.getName(), "Name should match.");
        assertEquals(modifiedErrorIds, resultMachineType.getErrors(), "Error list should reflect the modification.");
        assertEquals(2, resultMachineType.getErrors().size(), "Error list size should be 2 after modification.");
        assertTrue(resultMachineType.getErrors().containsAll(Arrays.asList(10, 30)), "Error list should contain 10 and 30.");
        assertFalse(resultMachineType.getErrors().contains(20), "Error list should not contain 20.");

        // Verify that the save method was called with the modifiedMachineType object
        verify(machineTypeRepository, times(1)).save(modifiedMachineType);
    }
}
