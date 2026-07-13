package com.FundRaise.F25.controller;


import com.FundRaise.F25.Service.FileStorageService;
import com.FundRaise.F25.Service.PdfExportService;
import com.FundRaise.F25.model.Contribution;
import org.springframework.transaction.annotation.Transactional;
import com.FundRaise.F25.model.Expense;
import com.FundRaise.F25.model.FundTarget;
import com.FundRaise.F25.model.K25User;
import com.FundRaise.F25.repository.ContributionRepository;
import com.FundRaise.F25.repository.ExpenseRepository;
import com.FundRaise.F25.repository.FundTargetRepository;
import com.FundRaise.F25.repository.K25UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final K25UserRepository userRepository;
    private final ContributionRepository contributionRepository;
    private final ExpenseRepository expenseRepository;
    private final FundTargetRepository targetRepository;
    private final FileStorageService fileStorageService;
    private final PdfExportService pdfExportService;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    public AdminController(K25UserRepository userRepository,
                            ContributionRepository contributionRepository,
                            ExpenseRepository expenseRepository,
                            FundTargetRepository targetRepository,
                            FileStorageService fileStorageService,
                            PdfExportService pdfExportService) {
        this.userRepository = userRepository;
        this.contributionRepository = contributionRepository;
        this.expenseRepository = expenseRepository;
        this.targetRepository = targetRepository;
        this.fileStorageService = fileStorageService;
        this.pdfExportService = pdfExportService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (!adminUsername.equals(username) || !adminPassword.equals(password)) {
            throw new IllegalArgumentException("Invalid admin credentials.");
        }
        return Map.of("success", true);
    }

    @GetMapping("/users")
    public List<K25User> users() {
        return userRepository.findAll();
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Boolean> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return Map.of("deleted", true);
    }

    private static LocalDateTime[] currentMonthRange() {
        LocalDate today = LocalDate.now();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = today.withDayOfMonth(today.lengthOfMonth()).atTime(23, 59, 59);
        return new LocalDateTime[]{monthStart, monthEnd};
    }

    private static LocalDateTime[] monthRange(Integer year, Integer month) {
        LocalDate reference = (year != null && month != null) ? LocalDate.of(year, month, 1) : LocalDate.now();
        LocalDateTime start = reference.withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = reference.withDayOfMonth(reference.lengthOfMonth()).atTime(23, 59, 59);
        return new LocalDateTime[]{start, end};
    }

    // Contribution Review — defaults to the current calendar month, so once a
    // new month starts the list (and therefore "This Month Income") naturally
    // reads empty again. Pass ?scope=all to pull full history.
    @Transactional(readOnly = true)
    @GetMapping("/contributions")
    public List<Map<String, Object>> contributions(@RequestParam(defaultValue = "month") String scope) {
        List<Contribution> source;
        if ("all".equalsIgnoreCase(scope)) {
            source = contributionRepository.findAllByOrderByCreatedAtDesc();
        } else {
            LocalDateTime[] range = currentMonthRange();
            source = contributionRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(range[0], range[1]);
        }
        return source.stream().map(c -> {
            K25User u = c.getUser();
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", c.getId());
            map.put("amount", c.getAmount());
            map.put("status", c.getStatus().name());
            map.put("createdAt", c.getCreatedAt());
            map.put("userId", u != null ? u.getId() : null);
            map.put("userName", u != null ? u.getFirstName() + " " + u.getLastName() : "Unknown");
            map.put("screenshotUrl", fileStorageService.toPublicUrl(c.getScreenshotPath()));
            return map;
        }).collect(Collectors.toList());
    }

    @PutMapping("/contributions/{id}/status")
    public Contribution updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Contribution c = contributionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contribution not found."));
        c.setStatus(Contribution.Status.valueOf(body.get("status")));
        return contributionRepository.save(c);
    }

    @DeleteMapping("/contributions/{id}")
    public Map<String, Boolean> deleteContribution(@PathVariable Long id) {
        contributionRepository.deleteById(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        List<Contribution> approved = contributionRepository.findByStatus(Contribution.Status.APPROVED);
        double totalIncome = approved.stream().mapToDouble(Contribution::getAmount).sum();
        double totalExpense = expenseRepository.findAll().stream().mapToDouble(Expense::getAmount).sum();
        double balanceCash = totalIncome - totalExpense;

        LocalDateTime[] range = currentMonthRange();
        double thisMonthIncome = contributionRepository
                .findByStatusAndCreatedAtBetween(Contribution.Status.APPROVED, range[0], range[1])
                .stream().mapToDouble(Contribution::getAmount).sum();

        return Map.of(
                "balanceCash", balanceCash,
                "thisMonthIncome", thisMonthIncome
        );
    }

    @PutMapping("/target")
    public FundTarget setTarget(@RequestBody Map<String, String> body) {
        FundTarget target = targetRepository.findById(1L).orElseGet(FundTarget::new);
        target.setId(1L);
        target.setAmount(Double.valueOf(body.get("amount")));
        return targetRepository.save(target);
    }

    @PostMapping("/expenses")
    public Expense addExpense(@RequestParam String description,
                               @RequestParam Double amount,
                               @RequestParam("screenshot") MultipartFile screenshot) {
        String filename = fileStorageService.store(screenshot);
        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setScreenshotPath(filename);
        return expenseRepository.save(expense);
    }

    // Expense Log — defaults to the current calendar month. Pass ?scope=all
    // for full history.
    @GetMapping("/expenses")
    public List<Map<String, Object>> expenses(@RequestParam(defaultValue = "month") String scope) {
        List<Expense> source;
        if ("all".equalsIgnoreCase(scope)) {
            source = expenseRepository.findAllByOrderByCreatedAtDesc();
        } else {
            LocalDateTime[] range = currentMonthRange();
            source = expenseRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(range[0], range[1]);
        }
        return source.stream().map(e -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", e.getId());
            map.put("description", e.getDescription());
            map.put("amount", e.getAmount());
            map.put("createdAt", e.getCreatedAt());
            map.put("screenshotUrl", fileStorageService.toPublicUrl(e.getScreenshotPath()));
            return map;
        }).collect(Collectors.toList());
    }

    // ── Monthly PDF exports ──
    // Defaults to the current month; pass ?year=2026&month=6 to pull an
    // earlier month (e.g. once July starts, June's records still export fine
    // even though they've dropped out of the "month"-scoped views above).

    @Transactional(readOnly = true)
    @GetMapping("/export/contributions")
    public ResponseEntity<byte[]> exportContributions(@RequestParam(required = false) Integer year,
                                                        @RequestParam(required = false) Integer month) {
        LocalDateTime[] range = monthRange(year, month);
        LocalDate reference = range[0].toLocalDate();
        String monthLabel = reference.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        List<Contribution> contributions = contributionRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(range[0], range[1]);
        byte[] pdf = pdfExportService.generateContributionReport(monthLabel, contributions);

        String filename = monthLabel.replace(" ", "_") + "_Contribution_Report.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/export/expenses")
    public ResponseEntity<byte[]> exportExpenses(@RequestParam(required = false) Integer year,
                                                   @RequestParam(required = false) Integer month) {
        LocalDateTime[] range = monthRange(year, month);
        LocalDate reference = range[0].toLocalDate();
        String monthLabel = reference.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        List<Expense> expenses = expenseRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(range[0], range[1]);
        byte[] pdf = pdfExportService.generateExpenseReport(monthLabel, expenses);

        String filename = monthLabel.replace(" ", "_") + "_Expense_Report.pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}