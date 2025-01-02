package com.project.paymentservice.payment.entity;

public enum PaymentStatusEnum {
    //결제 중, 결제 완료, 결제 실패
    IN_PROGRESS(PaymentStatus.IN_PROGRESS),
    COMPLETE(PaymentStatus.COMPLETE),
    FAIL(PaymentStatus.FAIL);

    private final String status;

    PaymentStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public static class PaymentStatus {
        public static final String IN_PROGRESS = "PAYMENT_IN_PROGRESS";
        public static final String COMPLETE = "PAYMENT_COMPLETE";
        public static final String FAIL = "PAYMENT_FAIL";
    }
}
