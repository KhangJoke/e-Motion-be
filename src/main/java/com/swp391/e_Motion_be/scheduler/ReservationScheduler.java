package com.swp391.e_Motion_be.scheduler;

import com.swp391.e_Motion_be.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    // Chạy mỗi 5 phút
    @Scheduled(fixedRate = 300000)
    public void checkExpiringReservations() {
        reservationService.notifyExpiringReservations();
    }

    @Scheduled(fixedRate = 300000)
    public void checkOverdueReservations() {
        reservationService.notifyOverdueReservations();
    }

    @Scheduled(fixedRate = 300000)
    public void checkCancelReservation() {
        reservationService.notifyCancelReservations();
    }
}
