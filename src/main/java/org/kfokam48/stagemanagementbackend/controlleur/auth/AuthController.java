package org.kfokam48.stagemanagementbackend.controlleur.auth;

import jakarta.validation.Valid;
import org.kfokam48.stagemanagementbackend.dto.auth.LoginRequest;
import org.kfokam48.stagemanagementbackend.service.auth.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:4200"}, allowedHeaders = "*", methods ={RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/api/auth")
public class AuthController {


    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> authenticateUser(@Valid @RequestBody LoginRequest authRequest) {
        try {
            String token = authService.authenticateUser(authRequest);
            String role = String.valueOf(authService.getUserRole(authRequest));
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            response.put ("role", role);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());

            return ResponseEntity.status(401).body(error);
        }


    }
}