package com.edoc.service;

import com.edoc.model.Document;
import com.edoc.model.Role;
import com.edoc.model.ShareDocument;
import com.edoc.repository.DocumentRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository docRepo;

    public JSONObject createDocument(Document document) throws Exception {

        Document findDoc = docRepo.findByOwnerAndTitle(document.getOwner(), document.getTitle());

        if (findDoc != null) {
            return Utility.getErrorResponse("Document already exists, Please chose a different title", HttpStatus.CONFLICT);
        }
        try {
            docRepo.save(document);
            return Utility.getResponse("", "Document created successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return Utility.getErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public JSONObject listDocumentsOfUser(String userId) {

        List<Document> documents = docRepo.findByOwner(userId);
        if (documents.isEmpty()) {
            return Utility.NO_DATA_AVAILABLE();
        }
        return Utility.getResponse("documents", new JSONArray().add(documents), HttpStatus.OK);
    }

    public JSONObject getDocumentOfUser(String userId, String docId) {

        Document document = docRepo.findByUserIdAndDocId(docId, userId);

        if (document == null) {
            return Utility.NO_DATA_AVAILABLE();
        }

        return Utility.getResponse("document", document, HttpStatus.OK);
    }

    public JSONObject shareDocument(ShareDocument requestBody) {

        Optional<Document> findDoc = docRepo.findById(requestBody.getDocumentId());

        if (findDoc.isEmpty()) {
            Utility.NO_DATA_AVAILABLE();
        }

        Document document = findDoc.get();
        if (!document.getOwner().equals(requestBody.getOwner())) {
            return Utility.getErrorResponse("User has no access", HttpStatus.UNAUTHORIZED);
        }

        String recipientEmail = requestBody.getRecipientEmail();
        List<String> accessTypeList = requestBody.getAccessType();

        //Check for valid access type
        for (String access : accessTypeList) {
            if (!isValidAccessType(access)) {
                return Utility.getErrorResponse("Not a valid access type", HttpStatus.NOT_ACCEPTABLE);
            }
        }

        Map<String, List<String>> collaborators = document.getCollaborators();
        if (collaborators == null) {
            collaborators = new LinkedHashMap<>();
        }

        if (collaborators.containsKey(recipientEmail)) {
            List<String> existingAccess = collaborators.get(recipientEmail);
            for (String access : accessTypeList) {
                if (existingAccess.contains(access)) {
                    return Utility.getErrorResponse("User already has the access", HttpStatus.CONFLICT);
                } else {
                    existingAccess.add(access);
                }
            }
            collaborators.put(recipientEmail, existingAccess);

        } else {
            collaborators.put(recipientEmail, accessTypeList);
        }

        document.setCollaborators(collaborators);
        try {
            docRepo.save(document);
            return Utility.getResponse("", "Document shared successfully", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return Utility.getErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private boolean isValidAccessType(String accessType) {

        for (Role type : Role.values()) {
            if (type.name().equalsIgnoreCase(accessType)) {
                return true;
            }
        }
        return false;
    }

    public JSONObject listDocuments() {
        List<Document> documentList = docRepo.findAll();
        if (documentList.isEmpty()) {
            return Utility.NO_DATA_AVAILABLE();
        }
        return Utility.getResponse("documents", new JSONArray().add(documentList), HttpStatus.OK);
    }
}
