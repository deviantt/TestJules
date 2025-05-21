package com.example.demo.service;

import com.example.demo.model.MachineType;
import com.example.demo.repository.MachineErrorRepository;
import com.example.demo.repository.MachineTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MachineTypeServiceTest {

    @Mock
    private MachineTypeRepository machineTypeRepository;

    @Mock
    private MachineErrorRepository machineErrorRepository; // Autowired in service, so needs to be mocked

    @InjectMocks
    private MachineTypeService machineTypeService;

    @Test
    void getAllMachineTypes_shouldCallRepositoryFindAll() {
        MachineType type1 = new MachineType("id1", "TypeA", "fw1.0", Collections.emptyList());
        MachineType type2 = new MachineType("id2", "TypeB", "fw2.0", Arrays.asList(101, 102));
        when(machineTypeRepository.findAll()).thenReturn(Arrays.asList(type1, type2));

        List<MachineType> types = machineTypeService.getAllMachineTypes();

        assertEquals(2, types.size());
        verify(machineTypeRepository, times(1)).findAll();
    }

    @Test
    void getMachineTypeByName_whenTypeExists_shouldReturnMachineType() {
        String name = "TypeA";
        MachineType typeA = new MachineType("id1", name, "fw1.0", Collections.emptyList());
        when(machineTypeRepository.findByName(name)).thenReturn(Optional.of(typeA));

        Optional<MachineType> foundType = machineTypeService.getMachineTypeByName(name);

        assertTrue(foundType.isPresent());
        assertEquals(name, foundType.get().getName());
        verify(machineTypeRepository, times(1)).findByName(name);
    }

    @Test
    void getMachineTypeByName_whenTypeNotFound_shouldReturnEmptyOptional() {
        String name = "NonExistentType";
        when(machineTypeRepository.findByName(name)).thenReturn(Optional.empty());

        Optional<MachineType> foundType = machineTypeService.getMachineTypeByName(name);

        assertFalse(foundType.isPresent());
        verify(machineTypeRepository, times(1)).findByName(name);
    }

    @Test
    void saveMachineType_shouldCallRepositorySave() {
        MachineType newType = new MachineType(null, "TypeC", "fw3.0", Arrays.asList(201));
        MachineType savedType = new MachineType("id3", "TypeC", "fw3.0", Arrays.asList(201));
        when(machineTypeRepository.save(any(MachineType.class))).thenReturn(savedType);

        MachineType result = machineTypeService.saveMachineType(newType);

        assertNotNull(result.getId());
        assertEquals(savedType.getName(), result.getName());
        verify(machineTypeRepository, times(1)).save(newType);
    }
}
