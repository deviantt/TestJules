package com.example.demo.controller;

import com.example.demo.model.MachineError;
import com.example.demo.model.MachineType;
import com.example.demo.service.MachineErrorService;
import com.example.demo.service.MachineTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MachineTypeController.class)
public class MachineTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MachineTypeService machineTypeService;

    @MockBean
    private MachineErrorService machineErrorService;

    @Test
    void viewMachineTypeErrors_withoutNameParam_shouldLoadDefaults() throws Exception {
        MachineType type1 = new MachineType("id1", "TypeA", "fw1.0", Collections.emptyList());
        MachineError error1 = new MachineError("errId1", 101, "Error 101", 101, 1, "High", "SysErr", "System1");
        when(machineTypeService.getAllMachineTypes()).thenReturn(Arrays.asList(type1));
        when(machineErrorService.getAllErrors()).thenReturn(Arrays.asList(error1));

        mockMvc.perform(get("/type"))
                .andExpect(status().isOk())
                .andExpect(view().name("machine-type-errors"))
                .andExpect(model().attributeExists("machineTypes", "allErrors"))
                .andExpect(model().attribute("assignedErrorsFull", Collections.emptyList())); // No name, so no specific assigned errors

        verify(machineTypeService, times(1)).getAllMachineTypes();
        verify(machineErrorService, times(1)).getAllErrors();
    }

    @Test
    void viewMachineTypeErrors_withNameParam_typeExists() throws Exception {
        String typeName = "TypeA";
        List<Integer> errorUniqueIds = Arrays.asList(101, 102);
        MachineType selectedType = new MachineType("id1", typeName, "fw1.0", errorUniqueIds);
        MachineError error101 = new MachineError("errId1", 101, "Error 101", 101, 1, "High", "SysErr", "System1");
        MachineError error102 = new MachineError("errId2", 102, "Error 102", 102, 2, "Med", "AppErr", "System2");
        List<MachineError> allErrorsList = Arrays.asList(error101, error102, new MachineError("errId3", 103, "Error 103", 103, 3, "Low", "NetErr", "System3"));

        when(machineTypeService.getAllMachineTypes()).thenReturn(Arrays.asList(selectedType)); // For dropdown population
        when(machineErrorService.getAllErrors()).thenReturn(allErrorsList); // For available errors
        when(machineTypeService.getMachineTypeByName(typeName)).thenReturn(Optional.of(selectedType));
        when(machineErrorService.getErrorByUniqueId(101)).thenReturn(Optional.of(error101));
        when(machineErrorService.getErrorByUniqueId(102)).thenReturn(Optional.of(error102));

        List<MachineError> expectedAssignedErrors = Arrays.asList(error101, error102);

        mockMvc.perform(get("/type").param("name", typeName))
                .andExpect(status().isOk())
                .andExpect(view().name("machine-type-errors"))
                .andExpect(model().attributeExists("machineTypes", "allErrors", "selectedMachineType", "assignedErrorsFull"))
                .andExpect(model().attribute("selectedMachineType", selectedType))
                .andExpect(model().attribute("assignedErrorsFull", expectedAssignedErrors));

        verify(machineTypeService, times(1)).getMachineTypeByName(typeName);
        verify(machineErrorService, times(1)).getErrorByUniqueId(101);
        verify(machineErrorService, times(1)).getErrorByUniqueId(102);
    }

    @Test
    void viewMachineTypeErrors_withNameParam_typeNotFound() throws Exception {
        String typeName = "NonExistentType";
        when(machineTypeService.getAllMachineTypes()).thenReturn(Collections.emptyList());
        when(machineErrorService.getAllErrors()).thenReturn(Collections.emptyList());
        when(machineTypeService.getMachineTypeByName(typeName)).thenReturn(Optional.empty());

        mockMvc.perform(get("/type").param("name", typeName))
                .andExpect(status().isOk())
                .andExpect(view().name("machine-type-errors"))
                .andExpect(model().attributeDoesNotExist("selectedMachineType"))
                .andExpect(model().attribute("assignedErrorsFull", Collections.emptyList()));

        verify(machineTypeService, times(1)).getMachineTypeByName(typeName);
    }

    @Test
    void saveMachineTypeErrors_typeExists() throws Exception {
        String typeName = "TypeA";
        List<Integer> errorIdsToSave = Arrays.asList(101, 103);
        MachineType existingType = new MachineType("id1", typeName, "fw1.0", Arrays.asList(101, 102));

        when(machineTypeService.getMachineTypeByName(typeName)).thenReturn(Optional.of(existingType));
        when(machineTypeService.saveMachineType(any(MachineType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/type/save")
                .param("name", typeName)
                .param("errorUniqueIds", "101", "103"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/type?name=" + typeName));

        verify(machineTypeService, times(1)).getMachineTypeByName(typeName);
        verify(machineTypeService, times(1)).saveMachineType(argThat(mt ->
            mt.getName().equals(typeName) && mt.getErrors().equals(errorIdsToSave)
        ));
    }
    
    @Test
    void saveMachineTypeErrors_typeExists_noErrorIdsParam() throws Exception {
        String typeName = "TypeA";
        MachineType existingType = new MachineType("id1", typeName, "fw1.0", Arrays.asList(101, 102));

        when(machineTypeService.getMachineTypeByName(typeName)).thenReturn(Optional.of(existingType));
        when(machineTypeService.saveMachineType(any(MachineType.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/type/save")
                .param("name", typeName)) // No errorUniqueIds param
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/type?name=" + typeName));

        verify(machineTypeService, times(1)).getMachineTypeByName(typeName);
        verify(machineTypeService, times(1)).saveMachineType(argThat(mt ->
            mt.getName().equals(typeName) && mt.getErrors().equals(Collections.emptyList())
        ));
    }


    @Test
    void saveMachineTypeErrors_typeNotFound() throws Exception {
        String typeName = "NonExistentType";
        when(machineTypeService.getMachineTypeByName(typeName)).thenReturn(Optional.empty());

        mockMvc.perform(post("/type/save")
                .param("name", typeName)
                .param("errorUniqueIds", "101"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/type?name=" + typeName));

        verify(machineTypeService, times(1)).getMachineTypeByName(typeName);
        verify(machineTypeService, never()).saveMachineType(any(MachineType.class));
    }

    @Test
    void showCreateMachineTypeForm_shouldShowForm() throws Exception {
        mockMvc.perform(get("/type/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-machine-type"))
                .andExpect(model().attributeExists("newMachineType"));
    }

    @Test
    void createMachineType_shouldCallServiceAndRedirect() throws Exception {
        MachineType newMachineType = new MachineType(null, "TypeD", "fw4.0", Collections.emptyList());
        when(machineTypeService.saveMachineType(any(MachineType.class)))
            .thenReturn(new MachineType("id4", "TypeD", "fw4.0", Collections.emptyList()));

        mockMvc.perform(post("/type/create")
                .flashAttr("newMachineType", newMachineType))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/type?name=" + newMachineType.getName()));

        verify(machineTypeService, times(1)).saveMachineType(any(MachineType.class));
    }
}
