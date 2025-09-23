package com.swp391.e_Motion_be.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;

@Data
@Entity
public class InvalidatedToken {
    @Id
    private String id;
    private Date expiryTime;

    public InvalidatedToken() {
    }

    public InvalidatedToken(String id, Date expiryTime) {
        this.id = id;
        this.expiryTime = expiryTime;
    }
}
