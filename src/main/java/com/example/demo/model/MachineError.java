package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "all_errors")
public class MachineError {

    @Id
    private String id;
    private Integer uniqueId;
    private String description;
    private Integer spn;
    private Integer fmi;
    private String level;
    private String type;
    private String system;
}
