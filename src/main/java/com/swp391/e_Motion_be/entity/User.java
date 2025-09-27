package com.swp391.e_Motion_be.entity;

import com.swp391.e_Motion_be.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long id;
    @Column(name = "full_name", nullable = false)
    private String fullName;
    @Column(name = "phone", nullable = false)
    private String phone;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(nullable = false, name = "enabled")
    private boolean enabled;
    @Column(name = "create_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createAt;
    @Column(name= "verification_code")
    private String verificationCode;
    @Column(name= "verification_code_expires_at")
    private LocalDateTime verificationCodeExpiresAt;
    @Column(name = "forgot_password_code")
    private String forgotPasswordCode;
    @Column(name = "forgot_password_code_expires_at")
    private LocalDateTime forgotPasswordCodeExpiresAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Staff staff;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Document> documents;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Deposit> deposits;

    public User() {
        this.createAt = LocalDateTime.now();
    }

    public User(String fullName, String email, String password, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getUsername() {
        return email;
    }
}