package com.edoc.controller;

import com.edoc.model.User;
import com.edoc.model.UserCredential;
import com.edoc.service.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/register")
    public User resisterUser(@RequestBody User user) {
        return userService.userResister(user);
    }

    @PatchMapping("/{id}")
    public User updateUserCredential(@RequestBody UserCredential userCredential, @PathVariable String id) {
        return userService.updateUserCredential(id, userCredential);
    }

    @PostMapping("/login")
    public String LoginUser(@RequestBody UserCredential userCredential) {
        return userService.verifyUser(userCredential);
    }

}
