package com.tanhung.antifraudsystem;

import com.tanhung.antifraudsystem.model.Role;
import com.tanhung.antifraudsystem.model.User;
import com.tanhung.antifraudsystem.repo.UserRepo;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
import lombok.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AntiFraudSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(AntiFraudSystemApplication.class, args);
    }


}
