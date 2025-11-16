package com.swp391.e_Motion_be.enums;

public enum ReservationStatus {
    PENDING, //set when just create new reservation
    FAILED ,
    CONFIRM,
    OVERDUE,
    CANCELLED,
    COMPLETED //set when rental create by reservation is completed
}
