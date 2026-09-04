package com.finance.tracker.auth.domain.entity;

import com.finance.tracker.auth.domain.DashboardMode;
import com.finance.tracker.auth.domain.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;
    private String name;

    private String role = "ROLE_USER"; // ROLE_USER, ROLE_ADMIN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType = UserType.REGULAR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DashboardMode dashboardMode = DashboardMode.EXPENSE_ONLY;

    public User(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole();
        this.userType = user.getUserType();
        this.dashboardMode = user.getDashboardMode();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
