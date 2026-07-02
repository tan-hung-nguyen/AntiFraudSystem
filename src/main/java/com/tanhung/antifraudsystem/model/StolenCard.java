package com.tanhung.antifraudsystem.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;


@Entity
@Table(name = "stolencards")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StolenCard{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Pattern(regexp = "^\\d{16}$", message = "Card number must be exactly 16 digits")
    @NotNull(message = "Your card number must not be null!")
    @Column(nullable = false, unique = true)
    private String cardNumber;

}
