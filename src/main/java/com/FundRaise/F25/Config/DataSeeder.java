package com.FundRaise.F25.Config;

import com.FundRaise.F25.model.FundTarget;
import com.FundRaise.F25.repository.FundTargetRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final FundTargetRepository targetRepository;

    public DataSeeder(FundTargetRepository targetRepository) {
        this.targetRepository = targetRepository;
    }

    @Override
    public void run(String... args) {
        if (targetRepository.findById(1L).isEmpty()) {
            FundTarget target = new FundTarget();
            target.setId(1L);
            target.setAmount(2500.0);
            targetRepository.save(target);
        }
    }
}
