package com.edoc.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ShareDocument {

    private String documentId;

    private String owner;

    private String recipientEmail;

    private List<String> accessType;

}
