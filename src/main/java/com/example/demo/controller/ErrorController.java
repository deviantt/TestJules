package com.example.demo.controller;

import com.example.demo.exception.ErrorAssignedException;
import com.example.demo.model.MachineError;
import com.example.demo.service.MachineErrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/all")
public class ErrorController {

    @Autowired
    private MachineErrorService machineErrorService;

    @GetMapping("")
    public String viewAllErrors(Model model) {
        model.addAttribute("errors", machineErrorService.getAllErrors());
        // Assuming 'all-errors.html' will have a form for adding new errors
        model.addAttribute("newError", new MachineError());
        return "all-errors";
    }

    @PostMapping("/add")
    public String addError(@ModelAttribute MachineError error) {
        machineErrorService.addError(error);
        return "redirect:/all";
    }

    @PostMapping("/delete/{id}")
    public String deleteError(@PathVariable String id) {
        try {
            machineErrorService.deleteError(id);
            return "redirect:/all";
        } catch (ErrorAssignedException e) {
            return "redirect:/all?error=assigned";
        }
    }

    // Shows the form for editing an error
    @GetMapping("/edit/{id}")
    public String showEditErrorForm(@PathVariable String id, Model model) {
        // The service's updateError method expects the Mongo ID.
        // If we need to fetch by uniqueId for display before edit, we'd need a service method like getErrorById(String mongoId)
        // For now, let's assume we fetch the error by its MongoDB ID for editing.
        // The updateError method in the service currently fetches by ID.
        // Let's add a new method to service to getErrorById(String id)
        Optional<MachineError> errorOptional = machineErrorService.getErrorById(id); // Assumes getErrorById(String id) exists or will be added
        if (errorOptional.isPresent()) {
            model.addAttribute("error", errorOptional.get());
            return "edit-error"; // Name of the Thymeleaf template for editing
        } else {
            // Handle error not found, perhaps redirect with an error message
            return "redirect:/all?errorNotFound=true";
        }
    }

    @PostMapping("/update/{id}")
    public String updateError(@PathVariable String id, @ModelAttribute MachineError errorDetails) {
        machineErrorService.updateError(id, errorDetails);
        return "redirect:/all";
    }
}
