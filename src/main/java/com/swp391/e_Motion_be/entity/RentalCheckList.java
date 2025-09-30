package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.CheckType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rental_checklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RentalCheckList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_type")
    private CheckType checkType;

    @Column(name = "pic_url")
    private String imgUrl;

    @Column(name = "kilometers")
    private Long kilometers;

    @Column(name = "current_battery")
    private double currentBattery;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id")
    private Rental rental;
}
