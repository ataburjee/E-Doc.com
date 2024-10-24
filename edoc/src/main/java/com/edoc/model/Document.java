package com.edoc.model;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Table(name = "documents")
public class Document {

    @Id
    private String id;
    @Setter
    private String title;
    @Setter
    private String content;
    //User id
    private String owner;
    private long ct;
    private long lu;
    private String lub;
    //Map<userId, List<accessType>>
    @Convert(converter = MapConverter.class)
    private Map<String, List<String>> collaborators;

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

    public Document setLub(String lub) {
        this.lub = lub;
        return this;
    }

    public Document setCollaborators(Map<String, List<String>> collaborators) {
        this.collaborators = collaborators;
        return this;
    }

}
