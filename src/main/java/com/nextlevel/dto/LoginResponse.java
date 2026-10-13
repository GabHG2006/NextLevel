package com.nextlevel.dto;

import java.util.List;

public class LoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private String correo;
    private List<String> roles;

    public LoginResponse(String token, String correo, List<String> roles) {
        this.token = token;
        this.correo = correo;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
