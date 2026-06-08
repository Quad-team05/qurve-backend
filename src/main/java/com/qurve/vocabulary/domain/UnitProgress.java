package com.qurve.vocabulary.domain;

import com.qurve.user.domain.User;
import com.qurve.vocabulary.enums.UnitStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_unit_progress")
public class UnitProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long unitProgressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "unit_number", nullable = false)
    private Integer unitNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UnitStatus status;

    @Column(name = "level", length = 10, nullable = false)
    private String level;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void updateStatus(UnitStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
}
