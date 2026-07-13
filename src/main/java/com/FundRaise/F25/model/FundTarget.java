package com.FundRaise.F25.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fund_target")
@Getter
@Setter
@NoArgsConstructor
public class FundTarget {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private Double amount = 2500.0;
}

