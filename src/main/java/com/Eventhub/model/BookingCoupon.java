
package com.Eventhub.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class BookingCoupon {

    private Long id;
    private Long bookingId;
    private Long couponId;
    private BigDecimal discountAmount;
    private Timestamp appliedAt;

    public BookingCoupon() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Timestamp getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Timestamp appliedAt) {
        this.appliedAt = appliedAt;
    }

    @Override
    public String toString() {
        return "BookingCoupon{" +
                "id=" + id +
                ", bookingId=" + bookingId +
                ", couponId=" + couponId +
                ", discountAmount=" + discountAmount +
                ", appliedAt=" + appliedAt +
                '}';
    }
}

