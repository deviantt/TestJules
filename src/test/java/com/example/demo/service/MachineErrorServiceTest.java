package com.example.demo.service;

import com.example.demo.model.MachineError;
import com.example.demo.repository.MachineErrorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MachineErrorServiceTest {

    @Mock
    private MachineErrorRepository machineErrorRepository;

    @InjectMocks
    private MachineErrorService machineErrorService;

    @Test
    void getAllErrors_shouldCallRepositoryFindAll() {
        MachineError error1 = new MachineError("id1", 1, "Desc1", 100, 1, "High", "TypeA", "SystemX");
        MachineError error2 = new MachineError("id2", 2, "Desc2", 200, 2, "Medium", "TypeB", "SystemY");
        when(machineErrorRepository.findAll()).thenReturn(Arrays.asList(error1, error2));

        List<MachineError> errors = machineErrorService.getAllErrors();

        assertEquals(2, errors.size());
        verify(machineErrorRepository, times(1)).findAll();
    }

    @Test
    void addError_shouldCallRepositorySave() {
        MachineError newError = new MachineError(null, 3, "New Desc", 300, 3, "Low", "TypeC", "SystemZ");
        MachineError savedError = new MachineError("id3", 3, "New Desc", 300, 3, "Low", "TypeC", "SystemZ");
        when(machineErrorRepository.save(any(MachineError.class))).thenReturn(savedError);

        MachineError result = machineErrorService.addError(newError);

        assertNotNull(result.getId());
        assertEquals(savedError.getDescription(), result.getDescription());
        verify(machineErrorRepository, times(1)).save(newError);
    }

    @Test
    void deleteError_shouldCallRepositoryDeleteById() {
        String errorId = "id1";
        doNothing().when(machineErrorRepository).deleteById(errorId);

        machineErrorService.deleteError(errorId);

        verify(machineErrorRepository, times(1)).deleteById(errorId);
    }

    @Test
    void updateError_whenErrorExists_shouldUpdateAndSave() {
        String errorId = "id1";
        MachineError existingError = new MachineError(errorId, 1, "Old Desc", 100, 1, "High", "TypeA", "SystemX");
        MachineError errorDetails = new MachineError(null, 1, "Updated Desc", 100, 1, "Critical", "TypeA_Updated", "SystemX_Updated");
        // Only description, level, type, system should be updated by the service method

        when(machineErrorRepository.findById(errorId)).thenReturn(Optional.of(existingError));
        when(machineErrorRepository.save(any(MachineError.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MachineError updatedError = machineErrorService.updateError(errorId, errorDetails);

        assertNotNull(updatedError);
        assertEquals(errorDetails.getDescription(), updatedError.getDescription());
        assertEquals(errorDetails.getLevel(), updatedError.getLevel());
        assertEquals(errorDetails.getType(), updatedError.getType());
        assertEquals(errorDetails.getSystem(), updatedError.getSystem());
        // Ensure non-updatable fields remain unchanged
        assertEquals(existingError.getUniqueId(), updatedError.getUniqueId());
        assertEquals(existingError.getSpn(), updatedError.getSpn());
        assertEquals(existingError.getFmi(), updatedError.getFmi());

        verify(machineErrorRepository, times(1)).findById(errorId);
        verify(machineErrorRepository, times(1)).save(existingError);
    }

    @Test
    void updateError_whenErrorNotFound_shouldReturnNull() {
        String errorId = "nonExistentId";
        MachineError errorDetails = new MachineError(null, 1, "Updated Desc", 100, 1, "Critical", "TypeA_Updated", "SystemX_Updated");
        when(machineErrorRepository.findById(errorId)).thenReturn(Optional.empty());

        MachineError updatedError = machineErrorService.updateError(errorId, errorDetails);

        assertNull(updatedError);
        verify(machineErrorRepository, times(1)).findById(errorId);
        verify(machineErrorRepository, never()).save(any(MachineError.class));
    }

    @Test
    void getErrorByUniqueId_whenErrorExists_shouldReturnError() {
        Integer uniqueId = 123;
        MachineError error = new MachineError("id1", uniqueId, "Desc", 100, 1, "High", "TypeA", "SystemX");
        when(machineErrorRepository.findByUniqueId(uniqueId)).thenReturn(Optional.of(error));

        Optional<MachineError> foundError = machineErrorService.getErrorByUniqueId(uniqueId);

        assertTrue(foundError.isPresent());
        assertEquals(uniqueId, foundError.get().getUniqueId());
        verify(machineErrorRepository, times(1)).findByUniqueId(uniqueId);
    }

    @Test
    void getErrorByUniqueId_whenErrorNotFound_shouldReturnEmptyOptional() {
        Integer uniqueId = 404;
        when(machineErrorRepository.findByUniqueId(uniqueId)).thenReturn(Optional.empty());

        Optional<MachineError> foundError = machineErrorService.getErrorByUniqueId(uniqueId);

        assertFalse(foundError.isPresent());
        verify(machineErrorRepository, times(1)).findByUniqueId(uniqueId);
    }

    @Test
    void getErrorById_whenErrorExists_shouldReturnError() {
        String id = "mongoId1";
        MachineError error = new MachineError(id, 123, "Desc", 100, 1, "High", "TypeA", "SystemX");
        when(machineErrorRepository.findById(id)).thenReturn(Optional.of(error));

        Optional<MachineError> foundError = machineErrorService.getErrorById(id);

        assertTrue(foundError.isPresent());
        assertEquals(id, foundError.get().getId());
        verify(machineErrorRepository, times(1)).findById(id);
    }

    @Test
    void getErrorById_whenErrorNotFound_shouldReturnEmptyOptional() {
        String id = "nonExistentMongoId";
        when(machineErrorRepository.findById(id)).thenReturn(Optional.empty());

        Optional<MachineError> foundError = machineErrorService.getErrorById(id);

        assertFalse(foundError.isPresent());
        verify(machineErrorRepository, times(1)).findById(id);
    }
}
