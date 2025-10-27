package com.swp391.e_Motion_be.scheduler;

import com.swp391.e_Motion_be.service.RentalService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RentalScheduler {

    private final RentalService rentalService;

    // Chạy mỗi 5 phút
    @Scheduled(fixedRate = 300000)
    public void checkExpiringRentals() {
        rentalService.notifyExpiringRentals();
    }

    @Scheduled(fixedRate = 300000)
    public void checkOverdueRentals() {
        rentalService.notifyOverdueRentals();
    }

    @Scheduled(fixedRate = 300000)
    public void checkCancelRentals() {
        rentalService.notifyCancelRentals();
    }
}
