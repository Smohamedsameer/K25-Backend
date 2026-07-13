package com.FundRaise.F25.controller;

import com.FundRaise.F25.model.Contribution;
import com.FundRaise.F25.model.Expense;
import com.FundRaise.F25.repository.ContributionRepository;
import com.FundRaise.F25.repository.ExpenseRepository;
import com.FundRaise.F25.repository.FundTargetRepository;
import com.FundRaise.F25.repository.K25UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PublicController {

    private final FundTargetRepository targetRepository;
    private final ContributionRepository contributionRepository;
    private final ExpenseRepository expenseRepository;
    private final K25UserRepository userRepository;

    public PublicController(FundTargetRepository targetRepository,
                             ContributionRepository contributionRepository,
                             ExpenseRepository expenseRepository,
                             K25UserRepository userRepository) {
        this.targetRepository = targetRepository;
        this.contributionRepository = contributionRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        Double target = targetRepository.findById(1L).map(t -> t.getAmount()).orElse(2500.0);

        // "Raised So Far" = every approved contribution (all-time), minus every
        // logged expense — e.g. 200 + 100 contributed, then a 150 expense, shows 150.
        double totalApproved = contributionRepository.findByStatus(Contribution.Status.APPROVED)
                .stream().mapToDouble(Contribution::getAmount).sum();
        double totalExpenses = expenseRepository.findAll()
                .stream().mapToDouble(Expense::getAmount).sum();
        double raisedSoFar = totalApproved - totalExpenses;

        long contributors = userRepository.count();

        // True current-calendar-month figure (resets naturally once the month
        // rolls over — nothing to reset by hand). Separate from "Raised So Far"
        // above, which is all-time.
        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = today.withDayOfMonth(today.lengthOfMonth()).atTime(23, 59, 59);
        double thisMonthCollected = contributionRepository
                .findByStatusAndCreatedAtBetween(Contribution.Status.APPROVED, monthStart, monthEnd)
                .stream().mapToDouble(Contribution::getAmount).sum();

        // Key kept as "thisMonthIncome" — that's what Home.jsx's STAT_CARDS reads.
        return Map.of(
                "target", target,
                "thisMonthIncome", raisedSoFar,
                "thisMonthCollected", thisMonthCollected,
                "contributors", contributors
        );
    }
}