package com.edoc.service.security;

import com.edoc.model.User;
import com.edoc.model.UserCredential;
import com.edoc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository repo;

    @Autowired
    JWTService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authManager;

    public User userResister(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repo.save(user);
    }

    public User updateUserCredential(String id, UserCredential userCredential) {
        Optional<User> userOpt = repo.findById(id);
        if (userOpt.isEmpty()) return null;
        User user = userOpt.get();

        String username = userCredential.getUsername();
        String password = userCredential.getPassword();

        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
        }
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return repo.save(user);
    }

    public String verifyUser(UserCredential userCredential) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(userCredential.getUsername(), userCredential.getPassword()));
            return "Login Successful. Token: " + jwtService.generateToken(userCredential.getUsername());
        } catch (Exception e) {
            System.out.println("error = " + e.getMessage());
            return e.getMessage();
        }
    }
}