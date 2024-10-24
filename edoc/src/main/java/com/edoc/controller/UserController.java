package com.edoc.controller;

import com.edoc.model.User;
import com.edoc.model.UserCredential;
import com.edoc.repository.UserRepository;
import com.edoc.service.Utility;
import com.edoc.service.security.UserService;
import jakarta.validation.Valid;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> resisterUser(@Valid @RequestBody User user) throws Exception {
        return Utility.generateResponse(userService.registerUser(user));
    }

    /*@PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody User user) throws Exception {
        JSONObject response = userService.registerUser(user);

        // Directly return ResponseEntity instead of using Utility to check if validation errors are suppressed
        return new ResponseEntity<>(response.toString(), HttpStatus.ACCEPTED); // Modify status if needed
    }*/


    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUserCredential(@RequestBody UserCredential userCredential, @PathVariable String id) {
        return Utility.generateResponse(userService.updateUserCredential(id, userCredential));
    }

    @PostMapping("/login")
    public ResponseEntity<?> LoginUser(@RequestBody UserCredential userCredential) throws Exception {
        return Utility.generateResponse(userService.verifyUser(userCredential));
    }

    // For a sample response---
    @Autowired
    UserRepository userRepo;

    @GetMapping("")
    public List<User> ListUser() {
        return userRepo.findAll(Sort.by(Sort.Direction.DESC, "ct"));
    }

    @DeleteMapping("")
    public String deleteUsers() {
        userRepo.deleteAll();
        return "deleted";
    }

}
