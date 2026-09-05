package com.railway.reservation.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleAuthRequest {

    @NotBlank(message = "Google ID Token / Auth Code is required")
    private String idToken;

    private String email;
    private String name;

    public GoogleAuthRequest() {}

    public GoogleAuthRequest(String idToken, String email, String name) {
        this.idToken = idToken;
        this.email = email;
        this.name = name;
    }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
