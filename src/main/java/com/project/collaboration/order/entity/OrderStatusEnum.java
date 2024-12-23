package com.project.collaboration.order.entity;

public enum OrderStatusEnum {
    //주문완료, 배송중, 배송완료, 환불중, 환불완료
    ORDER_COMPLETE(OrderStatus.ORDER_COMPLETE),
    ORDER_CANCEL(OrderStatus.ORDER_CANCEL),
    IN_DELIVERY(OrderStatus.IN_DELIVERY),
    DELIVERY_COMPLETE(OrderStatus.DELIVERY_COMPLETE),
    IN_REFUND_DELIVERY(OrderStatus.IN_REFUND_DELIVERY),
    REFUND_COMPLETE(OrderStatus.REFUND_COMPLETE);

    private final String status;

    OrderStatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public static class OrderStatus {
        public static final String ORDER_COMPLETE = "ORDER_COMPLETE";
        public static final String ORDER_CANCEL = "ORDER_CANCEL_COMPLETE";
        public static final String IN_DELIVERY = "IN_DELIVERY";
        public static final String DELIVERY_COMPLETE = "DELIVERY_COMPLETE";
        public static final String IN_REFUND_DELIVERY = "IN_REFUND_DELIVERY";
        public static final String REFUND_COMPLETE = "REFUND_COMPLETE";
    }
}
