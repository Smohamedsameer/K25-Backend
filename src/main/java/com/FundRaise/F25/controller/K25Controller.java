package com.FundRaise.F25.controller;

import com.FundRaise.F25.model.K25User;
import com.FundRaise.F25.repository.K25UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/k25")
public class K25Controller {

	public final K25UserRepository userRepository;
	public final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${app.k25.max-users}")
    private int maxUsers;

    public K25Controller(K25UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public K25User register(@RequestBody Map<String, String> body) {
        long currentCount = userRepository.count();
        if (currentCount >= maxUsers) {
            throw new IllegalStateException("K25 is full — all " + maxUsers + " seats are taken.");
        }
        if (userRepository.existsByEmail(body.get("email"))) {
            throw new IllegalStateException("An account with this email already exists.");
        }

        K25User user = new K25User();
        user.setFirstName(body.get("firstName"));
        user.setLastName(body.get("lastName"));
        user.setPhone(body.get("phone"));
        user.setDob(body.get("dob"));
        user.setEmail(body.get("email"));
        user.setPassword(encoder.encode(body.get("password")));

        return userRepository.save(user);
    }

    @PostMapping("/login")
    public K25User login(@RequestBody Map<String, String> body) {
        K25User user = userRepository.findByEmail(body.get("email"))
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        if (!encoder.matches(body.get("password"), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return user;
    }

    @GetMapping("/{id}")
    public K25User getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return Map.of("deleted", true);
    }
}
