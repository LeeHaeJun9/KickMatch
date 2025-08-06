package com.example.kickmatch.domain;

public enum ReservationStatus {
    PENDING("대기"),
    CONFIRMED("확정"),
    CANCELED("취소");

    private final String displayName;

    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
