package com.qurve.vocabulary.enums;

public enum UnitStatus {
    BEFORE("학습전"),
    IN_PROGRESS("학습중"),
    COMPLETED("학습완료");

    private final String statusText;

    UnitStatus(String statusText) {
        this.statusText = statusText;
    }

    public String getStatusText() {
        return statusText;
    }
}