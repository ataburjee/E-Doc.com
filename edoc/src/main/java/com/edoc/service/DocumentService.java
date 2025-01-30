package com.edoc.service;

import com.edoc.model.Document;
import com.edoc.model.AccessType;
import com.edoc.model.ShareDocument;
import com.edoc.model.User;
import com.edoc.repository.DocumentRepository;
import com.edoc.repository.UserRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository docRepo;

    @Autowired
    private UserRepository userRepo;

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
            return Utility.NO_DATA_AVAILABLE();
        }

        Document document = findDoc.get();
        String owner = requestBody.getOwner();
        if (!document.getOwner().equals(owner)) {
            return Utility.getErrorResponse("User has no access", HttpStatus.UNAUTHORIZED);
        }

        String recipientEmail = requestBody.getRecipientEmail();
        User recipientUser = userRepo.findByUsername(recipientEmail);

        if (recipientUser == null) {
            return Utility.getErrorResponse("Recipient is not a Edoc user", HttpStatus.NOT_FOUND);
        }

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

    public JSONObject deleteDocument(String documentId) {
        Optional<Document> document = docRepo.findById(documentId);
        if (document.isEmpty()) {
            return Utility.NO_DATA_AVAILABLE();
        }
        String owner = document.get().getOwner();
        Optional<User> user = userRepo.findById(owner);

        if (user.isEmpty()) {
            return Utility.getErrorResponse("User does not exists", HttpStatus.NOT_FOUND);
        }
        String usernameFromDoc = user.get().getUsername();
        String usernameFromAuth = getAuthenticatedUsername();

        if (!usernameFromDoc.equals(usernameFromAuth)) {
            return Utility.getErrorResponse("No permission to delete others document", HttpStatus.BAD_REQUEST);
        }
        try {
            docRepo.deleteById(documentId);
            return Utility.getResponse("Document deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return Utility.getErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getAuthenticatedUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername(); // Returns the username (email)
        } else {
            return principal.toString();
        }
    }
}
