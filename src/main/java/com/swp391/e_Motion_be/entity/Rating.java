package com.swp391.e_Motion_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="rating_id")
    private Long id;

    @Column(name ="comment")
    private String comment;

    @Column(name="score",nullable = false)
    private int score;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "rental_id")
    private Rental rental;

    public Rating(String comment, int score, User user, Rental rental) {
        this.comment = comment;
        this.score = score;
        this.user = user;
        this.rental = rental;
    }
}
