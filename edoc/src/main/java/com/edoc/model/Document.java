package com.edoc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
public class Document {

    @Id
    private String id;
    @Setter
    @NotNull
    private String title;
    @Setter
    private String content;
    @NotNull(message = "Owner/userId is mandatory")
    private String userId;
    private long ct;
    private long lu;
    @Setter
    private String lub;
    //Map<userId, List<accessType>>
    // Shared users and their corresponding access types
    @Convert(converter = MapConverter.class)
    private Map<String, List<String>> collaborators;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    public Document setId(String id) {
        this.id = id;
        return this;
    }

    public Document setCt(long ct) {
        this.ct = ct;
        return this;
    }

    public Document setLu(long lu) {
        this.lu = lu;
        return this;
    }

    public Document setCollaborators(Map<String, List<String>> collaborators) {
        this.collaborators = collaborators;
        return this;
    }

}
