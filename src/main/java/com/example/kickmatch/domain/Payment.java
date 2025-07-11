package com.example.kickmatch.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    private String paymentMethod;  // 예: 카드, 계좌이체 등

    private Integer amount;        // 결제 금액 (원 단위)

    private String status;         // 결제 상태 (성공, 실패, 취소 등)

    private LocalDateTime paymentDate;

    @PrePersist
    public void onPayment() {
        this.paymentDate = LocalDateTime.now();
    }
}
