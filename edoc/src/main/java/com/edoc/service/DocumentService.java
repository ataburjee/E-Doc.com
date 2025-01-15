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
    DocumentRepository docRepo;

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

        //If recipient already has some access
        if (collaborators.containsKey(recipientEmail)) {
            //Getting accesses
            List<String> existingAccess = collaborators.get(recipientEmail);
            for (String access : accessTypeList) {
                //If the providing access already there
                if (existingAccess.contains(access)) {
                    return Utility.getErrorResponse("User already has the access", HttpStatus.CONFLICT);
                } else {
                    // If the access is not there then add it
                    existingAccess.add(access);
                }
            }
            collaborators.put(recipientEmail, existingAccess);

        } else {
            collaborators.put(recipientEmail, accessTypeList);
        }

        try {
            docRepo.save(document.setCollaborators(collaborators));
            return Utility.getResponse("", "Document shared successfully", HttpStatus.ACCEPTED);
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

        Document existingDocument = docRepo.findById(documentId).get();

        if (documentToUpdate.getTitle() != null) {
            String title = documentToUpdate.getTitle();
            if (!title.isEmpty()) {
                existingDocument.setTitle(title);
            }
        }

        if (documentToUpdate.getContent() != null) {
            String content = documentToUpdate.getContent();
            if (!content.isEmpty()) {
                existingDocument.setTitle(content);
            }
        }
        try {
            docRepo.save(existingDocument);
            return Utility.getResponse("Document updated successfully", HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
