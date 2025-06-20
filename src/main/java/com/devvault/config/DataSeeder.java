package com.devvault.config;

import com.devvault.model.Role;
import com.devvault.model.User;
import com.devvault.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = User.builder()
                        .username("Admin User")
                        .email("admin@devvault.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .build();

                User dev1 = User.builder()
                        .username("Dev One")
                        .email("dev1@devvault.com")
                        .password(passwordEncoder.encode("dev123"))
                        .role(Role.DEVELOPER)
                        .build();

                User dev2 = User.builder()
                        .username("Dev Two")
                        .email("dev2@devvault.com")
                        .password(passwordEncoder.encode("dev123"))
                        .role(Role.DEVELOPER)
                        .build();

                userRepository.saveAll(List.of(admin, dev1, dev2));
                System.out.println("✅ Seeded initial users.");
            }
        };
    }
}
