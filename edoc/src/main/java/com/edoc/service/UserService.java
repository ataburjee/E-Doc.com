package com.edoc.service;

import com.edoc.model.User;
import com.edoc.model.UserCredential;
import com.edoc.repository.UserRepository;
import com.edoc.service.security.JWTService;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepo;

    @Autowired
    JWTService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authManager;

    public JSONObject registerUser(User user) throws Exception {

        String username = user.getUsername();
        if (userRepo.existsByUsername(username)) {
            return Utility.getErrorResponse("Provided email id already registered", String.format("Email '%s' already exists!", username), HttpStatus.CONFLICT);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        try {
            user.setId(Utility.generateId());
            long ct = System.currentTimeMillis();
            user.setCt(ct);
            user.setLu(ct);
            userRepo.save(user);
            return Utility.getResponse("User registered successfully", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return Utility.getErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public JSONObject updateUserCredential(String id, UserCredential userCredential) {
        Optional<User> userOpt = userRepo.findById(id);
        if (userOpt.isEmpty()) {
            return Utility.getErrorResponse("User does not exists", String.format("Invalid userId %s", id), HttpStatus.NOT_FOUND);
        }
        User user = userOpt.get();

        String username = userCredential.getUsername();
        String password = userCredential.getPassword();

        if (username != null && !username.isEmpty()) {
            user.setUsername(username);
        }
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setLu(System.currentTimeMillis());
        try {
            userRepo.save(user);
            return Utility.getResponse("Updated successfully", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return Utility.getErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public JSONObject verifyUser(UserCredential userCredential) throws Exception {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(userCredential.getUsername(), userCredential.getPassword()));
        } catch (Exception e) {
            return Utility.getErrorResponse("Incorrect username/password", "Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        String username = userCredential.getUsername();
        User user = userRepo.findByUsername(username);
        return getLoginResponse(user, username);
    }

    private JSONObject getLoginResponse(User user, String username) throws Exception {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("user", user);
        map.put("token", jwtService.generateToken(username));
        return Utility.getResponse(map, HttpStatus.OK);
    }
}