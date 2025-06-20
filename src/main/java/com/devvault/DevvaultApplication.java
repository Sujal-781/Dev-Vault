package com.devvault;

import com.devvault.model.Role;
import com.devvault.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.devvault.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class DevvaultApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevvaultApplication.class, args);
	}


	@Bean
	public CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (!userRepository.existsByEmail("admin@example.com")) {
				User admin = User.builder()
						.username("admin")
						.email("admin@example.com")
						.password(passwordEncoder.encode("admin123"))
						.role(Role.ADMIN)
						.build();

				userRepository.save(admin);
				System.out.println("✅ Admin user seeded");
			}
		};
	}

}
