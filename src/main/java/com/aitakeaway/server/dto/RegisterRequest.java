package com.aitakeaway.server.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String phone;
    /** CUSTOMER 或 MERCHANT，不传默认 CUSTOMER */
    private String role;
}
