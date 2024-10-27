package com.edoc.service;

import com.edoc.model.Document;
import com.edoc.model.AccessType;
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

        Document findDoc = docRepo.findByOwnerAndTitle(document.getOwner(), document.getTitle().trim());

        if (findDoc != null) {
            System.out.println("doc found with the title...");
            return Utility.getErrorResponse("Document already exists, Please chose a different title", HttpStatus.CONFLICT);
        }
        try {
            long currentTime = System.currentTimeMillis();
            document.setId(Utility.generateId())
                    .setCt(currentTime)
                    .setLu(currentTime)
                    .setLub(document.getOwner());
            docRepo.save(document);
            return Utility.getResponse("Document created successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return Utility.getErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public JSONObject listDocumentsOfUser(String userId) {

        List<Document> documents = docRepo.findByOwner(userId);
        if (documents.isEmpty()) {
            return Utility.NO_DATA_AVAILABLE();
        }
        JSONArray jsonArray = new JSONArray();
        Collections.reverse(documents);
        jsonArray.add(documents);
        return Utility.getResponse("documents", jsonArray, HttpStatus.OK);
    }

    public JSONObject getDocumentOfUser(String userId, String docId) {

        Document document = docRepo.findByUserIdAndDocId(docId, userId);

        if (document == null) {
            return Utility.NO_DATA_AVAILABLE();
        }

        return Utility.getResponse("document", document, HttpStatus.OK);
    }

    public JSONObject shareDocument(String documentId, ShareDocument requestBody) {

        Optional<Document> findDoc = docRepo.findById(documentId);

        if (findDoc.isEmpty()) {
            Utility.NO_DATA_AVAILABLE();
        }

        Document document = findDoc.get();
        if (!document.getOwner().equals(requestBody.getOwner())) {
            return Utility.getErrorResponse("User has no access", HttpStatus.UNAUTHORIZED);
        }

        String recipientEmail = requestBody.getRecipientEmail();
        List<String> accessTypeList = requestBody.getAccessType();

        if (accessTypeList.isEmpty()) {
            return Utility.getErrorResponse("Please select an access type", "At least one access type has to provide", HttpStatus.BAD_REQUEST);
        }

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
            if (existingAccess.size() == AccessType.values().length - 1) {
                return Utility.getErrorResponse("User already has all the access", HttpStatus.BAD_REQUEST);
            }
            for (String access : accessTypeList) {
                if (existingAccess.contains(access)) {
                    return Utility.getErrorResponse("User already has the access \"" + access + "\"", HttpStatus.CONFLICT);
                } else {
                    existingAccess.add(access);
                }
            }
            collaborators.put(recipientEmail, existingAccess);

        } else {
            collaborators.put(recipientEmail, accessTypeList);
        }

        try {
            docRepo.save(document.setCollaborators(collaborators));
            return Utility.getResponse("Document shared successfully", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return Utility.getErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    private boolean isValidAccessType(String accessType) {

        for (AccessType type : AccessType.values()) {
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

        JSONArray docArray = new JSONArray();
        docArray.add(documentList);
        return Utility.getResponse("documents", docArray, HttpStatus.OK);
    }


    public JSONObject updateDocument(String documentId, Document documentToUpdate) throws Exception {
//        documentToUpdate =  owner/userId,
//                            title,
//                            content
        if (!docRepo.existsById(documentId)) {
            return Utility.getErrorResponse("Provide valid document id", "Document not found, documentId = " + documentId, HttpStatus.NOT_FOUND);
        }
        if (documentToUpdate.getOwner().isEmpty()) {
            return Utility.getErrorResponse("Please define document owner", HttpStatus.BAD_REQUEST);
        }

        String title = documentToUpdate.getTitle();
        if (title == null || title.isEmpty()) {
            return Utility.getErrorResponse("Please provide a title of the document", "Title not found", HttpStatus.BAD_REQUEST);
        }

        Document existingDocument = docRepo.findById(documentId).get();
        existingDocument.setTitle(title);

        String content = documentToUpdate.getContent();
        if (content != null && !content.isEmpty()) {
            existingDocument.setContent(content);
        }
        try {
            docRepo.save(existingDocument);
            return Utility.getResponse("Document updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
