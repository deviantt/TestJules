package com.example.demo.controller;

import com.example.demo.model.MachineError;
import com.example.demo.model.MachineType;
import com.example.demo.service.MachineErrorService;
import com.example.demo.service.MachineTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/type")
public class MachineTypeController {

    @Autowired
    private MachineTypeService machineTypeService;

    @Autowired
    private MachineErrorService machineErrorService;

    @GetMapping("")
    public String viewMachineTypeErrors(@RequestParam(name = "name", required = false) String name, Model model) {
        model.addAttribute("machineTypes", machineTypeService.getAllMachineTypes());
        model.addAttribute("allErrors", machineErrorService.getAllErrors()); // Add all available errors for selection

        if (name != null && !name.isEmpty()) {
            Optional<MachineType> selectedMachineTypeOpt = machineTypeService.getMachineTypeByName(name);
            if (selectedMachineTypeOpt.isPresent()) {
                MachineType selectedMachineType = selectedMachineTypeOpt.get();
                model.addAttribute("selectedMachineType", selectedMachineType);

                List<MachineError> assignedErrorsFull = selectedMachineType.getErrors().stream()
                        .map(machineErrorService::getErrorByUniqueId)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                model.addAttribute("assignedErrorsFull", assignedErrorsFull);
            } else {
                // Handle machine type not found, perhaps add a message to the model
                model.addAttribute("assignedErrorsFull", Collections.emptyList());
            }
        } else {
            model.addAttribute("assignedErrorsFull", Collections.emptyList());
        }
        return "machine-type-errors";
    }

    @PostMapping("/save")
    public String saveMachineTypeErrors(@RequestParam("name") String name,
                                        @RequestParam(name = "errorUniqueIds", required = false) List<Integer> errorUniqueIds) {
        Optional<MachineType> machineTypeOpt = machineTypeService.getMachineTypeByName(name);
        if (machineTypeOpt.isPresent()) {
            MachineType machineType = machineTypeOpt.get();
            if (errorUniqueIds == null) {
                machineType.setErrors(Collections.emptyList());
            } else {
                machineType.setErrors(errorUniqueIds);
            }
            machineTypeService.saveMachineType(machineType);
        } else {
            // Handle machine type not found - maybe create it or return an error
            // For now, we just redirect. Consider creating a new MachineType if it doesn't exist.
            // Or, more robustly, ensure the form only allows editing existing types or has a separate "create" flow.
        }
        return "redirect:/type?name=" + name;
    }

    // Optional: Add a method to show a form for creating a new machine type
    @GetMapping("/create")
    public String showCreateMachineTypeForm(Model model) {
        model.addAttribute("newMachineType", new MachineType());
        return "create-machine-type"; // Assuming a 'create-machine-type.html' template
    }

    // Optional: Add a method to handle the creation of a new machine type
    @PostMapping("/create")
    public String createMachineType(@ModelAttribute MachineType newMachineType) {
        // Basic save. Consider checking if a machine type with the same name already exists.
        machineTypeService.saveMachineType(newMachineType);
        return "redirect:/type?name=" + newMachineType.getName();
    }
}
