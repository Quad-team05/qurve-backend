package com.qurve.user.domain;

import com.qurve.global.entity.BaseEntity;
import com.qurve.global.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_user")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "login_id", length = 30, nullable = false, unique = true)
    private String loginId;

    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(name = "nickname", length = 30, nullable = false)
    private String nickname;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;
}
