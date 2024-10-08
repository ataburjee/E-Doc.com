package com.edoc.model;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;



@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "documents")
public class Document {

    @Id
    private String id;

    private String title;

    private String content;

    //User id
    private String owner;

    //Map<userId, List<accessType>>
    @Convert(converter = MapConverter.class)
    private Map<String, List<String>> collaborators;

}
