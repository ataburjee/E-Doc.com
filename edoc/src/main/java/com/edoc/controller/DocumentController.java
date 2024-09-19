package com.edoc.controller;

import com.edoc.model.Document;
import com.edoc.model.ShareDocument;
import com.edoc.service.DocumentService;
import com.edoc.service.Utility;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    //Create a document
    @PostMapping({"", "/"})
    public ResponseEntity<?> addDocument(@RequestBody Document document) throws Exception {
        JSONObject object = documentService.createDocument(document);
        return ResponseEntity.status((HttpStatusCode) object.get(Utility.STATUS_CODE)).body(object.get(Utility.RESPONSE));
    }

    @GetMapping({"", "/"})
    public ResponseEntity<?> getDocuments() throws Exception {
        JSONObject object = documentService.listDocuments();
        return ResponseEntity.status((HttpStatusCode) object.get(Utility.STATUS_CODE)).body(object.get(Utility.RESPONSE));
    }

    //Get all the documents for a particular user
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocuments(@RequestParam String id) throws Exception {
        JSONObject object = documentService.listDocumentsOfUser(id);
        return ResponseEntity.status((HttpStatusCode) object.get(Utility.STATUS_CODE)).body(object.get(Utility.RESPONSE));
    }

    //Get a particular document for an user
    @GetMapping("/{userId}")
    public ResponseEntity<?> getDocumentOfAnUser(@RequestParam("userId") String userId, @PathVariable String id) throws Exception {
        JSONObject object = documentService.getDocumentOfUser(userId, id);
        return ResponseEntity.status((HttpStatusCode) object.get(Utility.STATUS_CODE)).body(object.get(Utility.RESPONSE));
    }


    //Share document with a person
    @PatchMapping("/{id}/share")
    public ResponseEntity<?> shareDocument(@RequestBody ShareDocument document) throws Exception {
        JSONObject object = documentService.shareDocument(document);
        return ResponseEntity.status((HttpStatusCode) object.get(Utility.STATUS_CODE)).body(object.get(Utility.RESPONSE));
    }

}
