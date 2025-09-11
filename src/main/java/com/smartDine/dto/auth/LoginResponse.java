package com.smartDine.dto.auth;
public class LoginResponse {
    private String token;

    private long expiresIn;

    public LoginResponse(String token, long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }
    public LoginResponse() {
    }
    public String getToken() {
        return token;
    }

    public LoginResponse  setToken(String token) {
        this.token = token;
        return this ;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public LoginResponse setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
        return this ;
    }
}
