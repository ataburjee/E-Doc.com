package com.edoc.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@NotNull
public class RemoveAccess {
    private AccessType accessType;
    private String accessorUserId;
}
