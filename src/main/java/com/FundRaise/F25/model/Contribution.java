package com.FundRaise.F25.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "contributions")
@Getter
@Setter
@NoArgsConstructor
public class Contribution {

    public enum Status { PENDING, APPROVED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    public K25User user;

    @Column(nullable = false)
    public Double amount;

    public String screenshotPath;

    @Enumerated(EnumType.STRING)
    public Status status = Status.PENDING;

    public LocalDateTime createdAt = LocalDateTime.now();
}

