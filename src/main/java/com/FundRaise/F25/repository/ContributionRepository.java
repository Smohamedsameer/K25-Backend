package com.FundRaise.F25.repository;
import com.FundRaise.F25.model.Contribution;
import org.springframework.data.jpa.repository.JpaRepository;
 
import java.time.LocalDateTime;
import java.util.List;
 
public interface ContributionRepository extends JpaRepository<Contribution, Long> {
 
    List<Contribution> findByUserIdOrderByCreatedAtDesc(Long userId);
 
    List<Contribution> findAllByOrderByCreatedAtDesc();
 
    // Uses Contribution.Status — the actual type of Contribution.status —
    // not the unrelated ContributionStatus class.
    List<Contribution> findByStatus(Contribution.Status status);
 
    // Needed by AdminController.summary() and PublicController.summary()
    // for "this month's" income — was missing entirely before.
    List<Contribution> findByStatusAndCreatedAtBetween(Contribution.Status status, LocalDateTime start, LocalDateTime end);

    // Needed to scope the admin Contribution Review list to the current
    // calendar month regardless of status (pending/approved/rejected all show).
    List<Contribution> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);
}