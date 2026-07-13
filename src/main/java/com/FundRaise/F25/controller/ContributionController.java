package com.FundRaise.F25.controller;

import com.FundRaise.F25.Service.FileStorageService;
import com.FundRaise.F25.model.Contribution;
import com.FundRaise.F25.model.K25User;
import com.FundRaise.F25.repository.ContributionRepository;
import com.FundRaise.F25.repository.K25UserRepository;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/contributions")
public class ContributionController {

    private final ContributionRepository contributionRepository;
    private final K25UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ContributionController(ContributionRepository contributionRepository,
                                   K25UserRepository userRepository,
                                   FileStorageService fileStorageService) {
        this.contributionRepository = contributionRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping
    public Contribution submit(@RequestParam Long userId,
                                @RequestParam Double amount,
                                @RequestParam("screenshot") MultipartFile screenshot) {
        K25User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String filename = fileStorageService.store(screenshot);

        Contribution contribution = new Contribution();
        contribution.setUser(user);
        contribution.setAmount(amount);
        contribution.setScreenshotPath(filename);
        contribution.setStatus(Contribution.Status.PENDING);

        return contributionRepository.save(contribution);
    }

    // Was missing entirely — the User Dashboard needs this to show
    // "your contribution history" with each one's approval status.
    @GetMapping("/user/{userId}")
    public List<Contribution> getForUser(@PathVariable Long userId) {
        return contributionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}