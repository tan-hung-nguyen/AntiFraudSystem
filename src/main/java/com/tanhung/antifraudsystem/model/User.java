package com.tanhung.antifraudsystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username", "email", "phone_number"})
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 30)
    private String firstName;

    @Column(name =  "last_name", nullable = false, length = 30)
    private String lastName;

    @Column(nullable = false, length = 30)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    @Column(name = "phone_number", length = 10)
    private String phoneNumber;
}
