package com.tanhung.antifraudsystem.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = false)
public class UserRegistrationRequest {

    @JsonProperty("firstName")
    @NotEmpty(message = "First name can't be empty!")
    @Pattern(regexp = "^[\\p{L}\\s'-]+$",
            message = "First name must contain only letters!")
    @Size(min = 2, max = 30)
    private String firstName;

    @JsonProperty("lastName")
    @NotEmpty(message = "Last name can't be empty!")
    @Pattern(regexp = "^[\\p{L}\\s'-]+$",
            message = "Last name must contain only letters!")
    @Size(min = 2, max = 30)
    private String lastName;

    @JsonProperty("username")
    @NotEmpty(message = "Username can't be empty!")
    @Size(min = 5, max = 30)
    @Pattern(regexp = "^[a-zA-Z0-9._]+$",
            message = "Username must contain only letters, numbers, dot, underscore!")
    private String username;

    @JsonProperty("password")
    @NotEmpty(message = "Password can't be empty!")
    @Size(min = 5, max = 50)
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$",
            message = "Password must have at least 1 uppercase, 1 lowercase, 1 number!")
    private String password;

    @Email(message = "Invalid email format!")
    private String email;

    @Size(min = 10, max = 10)
    @Pattern(regexp = "^\\d+$", message = "Invalid phone number format!")
    private String phoneNumber;

    public void usernameToLowerCase(){
        this.username = username.toLowerCase();
    }

    public void emailToLowerCase(){
        this.email = email.toLowerCase();
    }
}
