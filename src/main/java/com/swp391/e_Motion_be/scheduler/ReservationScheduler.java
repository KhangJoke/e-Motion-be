package com.swp391.e_Motion_be.scheduler;

import com.swp391.e_Motion_be.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    @Scheduled(fixedRate = 6000000) // every hour
    public void notifyReservations() {
        reservationService.notificationReservation();
    }
}
