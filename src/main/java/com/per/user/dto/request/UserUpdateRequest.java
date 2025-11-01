package com.per.user.dto.request;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import com.per.auth.entity.RoleType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserUpdateRequest {

    @Size(min = 3, max = 50)
    private String username;

    @Email(message = "Email format is invalid")
    private String email;

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    private Boolean emailVerified;

    private Boolean active;

    @Size(min = 6, max = 100, message = "Password must contain at least 6 characters")
    private String password;

    private Set<RoleType> roles;
}
