package com.FundRaise.F25.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "k25_users")
@Getter
@Setter
@NoArgsConstructor
public class K25User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String firstName;

    @Column(nullable = false)
    public String lastName;

    @Column(nullable = false)
    public String phone;

    @Column(nullable = false)
    public String dob;

    @Column(nullable = false, unique = true)
    public String email;

    @JsonIgnore
    @Column(nullable = false)
    public String password;

    public LocalDateTime createdAt = LocalDateTime.now();

    // @JsonIgnore added — without it, Jackson tries to serialize this lazy
    // collection outside the Hibernate session and throws LazyInitializationException.
    // A user's contributions are fetched separately via
    // GET /api/contributions/user/{userId} instead.
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Contribution> contributions = new ArrayList<>();
}