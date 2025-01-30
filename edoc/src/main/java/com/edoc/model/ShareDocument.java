package com.edoc.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ShareDocument {

    @NotNull(message = "Provide a valid document id")
    private String documentId;
    @NotNull(message = "Provide a valid user id")
    private String owner;
    @NotNull(message = "Provide a valid recipient email")
    private String recipientEmail;
    @NotNull(message = "Provide a valid access type")
    private List<String> accessType;

}
