package com.FundRaise.F25.controller;



import com.FundRaise.F25.model.Contribution;

import com.FundRaise.F25.model.Expense;
import com.FundRaise.F25.repository.ContributionRepository;
import com.FundRaise.F25.repository.ExpenseRepository;
import com.FundRaise.F25.repository.FundTargetRepository;
import com.FundRaise.F25.repository.K25UserRepository;
import org.springframework.web.bind.annotation.*;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
 
    private final ContributionRepository contributionRepository;
    private final ExpenseRepository expenseRepository;
    private final FundTargetRepository targetRepository;
    private final K25UserRepository userRepository;
 
    public StatsController(ContributionRepository contributionRepository,
                            ExpenseRepository expenseRepository,
                            FundTargetRepository targetRepository,
                            K25UserRepository userRepository) {
        this.contributionRepository = contributionRepository;
        this.expenseRepository = expenseRepository;
        this.targetRepository = targetRepository;
        this.userRepository = userRepository;
    }
 
    private double totalApprovedContributions() {
        List<Contribution> approved = contributionRepository.findByStatus(Contribution.Status.APPROVED);
        double sum = 0.0;
        for (Contribution c : approved) {
            if (c.getAmount() != null) sum += c.getAmount();
        }
        return sum;
    }
 
    private double totalExpenses() {
        List<Expense> expenses = expenseRepository.findAll();
        double sum = 0.0;
        for (Expense e : expenses) {
            if (e.getAmount() != null) sum += e.getAmount();
        }
        return sum;
    }
 
    // Public — powers the Home page cards.
    // "Raised So Far" = every approved contribution added together, minus every
    // logged expense — e.g. 200 + 100 contributed, then a 150 expense, shows 150.
    @GetMapping("/summary")
    public Map<String, Object> summary() {
        Double target = targetRepository.findById(1L)
                .map(t -> t.getAmount())
                .orElse(2500.0);
 
        double raisedSoFar = totalApprovedContributions() - totalExpenses();
        long contributors = userRepository.count();
 
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("target", target);
        body.put("thisMonthIncome", raisedSoFar); // key name kept for frontend compatibility
        body.put("contributors", contributors);
        return body;
    }
 
    // Admin only — Balance Cash + This Month Income cards on Admin dashboard.
    // Balance Cash uses the exact same net formula as "Raised So Far" above.
    @GetMapping("/admin-summary")
    public Map<String, Object> adminSummary() {
        double totalApproved = totalApprovedContributions();
        double totalExpenses = totalExpenses();
        double balance = totalApproved - totalExpenses;
 
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("balanceCash", balance);
        body.put("thisMonthIncome", totalApproved); // gross approved contributions, before expenses
        return body;
    }
}