package com.example.demo.controller;

import com.example.demo.model.MachineError;
import com.example.demo.service.MachineErrorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ErrorController.class)
public class ErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MachineErrorService machineErrorService;

    @Test
    void viewAllErrors_shouldReturnAllErrorsView() throws Exception {
        MachineError error1 = new MachineError("id1", 1, "Desc1", 100, 1, "High", "TypeA", "SystemX");
        MachineError error2 = new MachineError("id2", 2, "Desc2", 200, 2, "Medium", "TypeB", "SystemY");
        when(machineErrorService.getAllErrors()).thenReturn(Arrays.asList(error1, error2));

        mockMvc.perform(get("/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("all-errors"))
                .andExpect(model().attributeExists("errors"))
                .andExpect(model().attribute("errors", Arrays.asList(error1, error2)))
                .andExpect(model().attributeExists("newError"));

        verify(machineErrorService, times(1)).getAllErrors();
    }

    @Test
    void addError_shouldCallServiceAndRedirect() throws Exception {
        MachineError newError = new MachineError(null, 3, "New Desc", 300, 3, "Low", "TypeC", "SystemZ");
        when(machineErrorService.addError(any(MachineError.class))).thenReturn(new MachineError("id3", 3, "New Desc", 300, 3, "Low", "TypeC", "SystemZ"));

        mockMvc.perform(post("/all/add")
                .flashAttr("machineError", newError)) // Use flashAttr for @ModelAttribute
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/all"));

        verify(machineErrorService, times(1)).addError(any(MachineError.class));
    }

    @Test
    void deleteError_shouldCallServiceAndRedirect() throws Exception {
        String errorId = "id1";
        doNothing().when(machineErrorService).deleteError(errorId);

        mockMvc.perform(post("/all/delete/{id}", errorId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/all"));

        verify(machineErrorService, times(1)).deleteError(errorId);
    }

    @Test
    void showEditErrorForm_whenErrorExists_shouldShowForm() throws Exception {
        String errorId = "id1";
        MachineError existingError = new MachineError(errorId, 1, "Desc1", 100, 1, "High", "TypeA", "SystemX");
        when(machineErrorService.getErrorById(errorId)).thenReturn(Optional.of(existingError));

        mockMvc.perform(get("/all/edit/{id}", errorId))
                .andExpect(status().isOk())
                .andExpect(view().name("edit-error"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("error", existingError));

        verify(machineErrorService, times(1)).getErrorById(errorId);
    }

    @Test
    void showEditErrorForm_whenErrorNotFound_shouldRedirectWithError() throws Exception {
        String errorId = "nonExistentId";
        when(machineErrorService.getErrorById(errorId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/all/edit/{id}", errorId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/all?errorNotFound=true"));

        verify(machineErrorService, times(1)).getErrorById(errorId);
    }

    @Test
    void updateError_shouldCallServiceAndRedirect() throws Exception {
        String errorId = "id1";
        MachineError errorDetails = new MachineError(null, 1, "Updated Desc", 100, 1, "Critical", "TypeA_Updated", "SystemX_Updated");
        // The ID in errorDetails is null, but the path variable id is used by service
        when(machineErrorService.updateError(eq(errorId), any(MachineError.class)))
                .thenReturn(new MachineError(errorId, 1, "Updated Desc", 100, 1, "Critical", "TypeA_Updated", "SystemX_Updated"));

        mockMvc.perform(post("/all/update/{id}", errorId)
                .flashAttr("machineError", errorDetails))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/all"));

        verify(machineErrorService, times(1)).updateError(eq(errorId), any(MachineError.class));
    }
}
