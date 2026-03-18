package com.tanhung.antifraudsystem.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationRequest {

    @JsonProperty("firstName")
    @NotEmpty(message = "First name can't be empty!")
    @Length(min = 2, max = 15)
    private String firstName;

    @JsonProperty("lastName")
    @NotEmpty(message = "Last name can't be empty!")
    @Length(min = 2, max = 15)
    private String lastName;

    @JsonProperty("username")
    @NotEmpty(message = "Username can't be empty!")
    @Length(min = 5, max = 30)
    private String username;

    @JsonProperty("password")
    @NotEmpty(message = "Password can't be empty!")
    @Length(min = 5, max = 50)
    private String password;

    @Email(message = "Invalid email format!")
    private String email;

    @Length(min = 10, max = 10)
    private String phoneNumber;
}
