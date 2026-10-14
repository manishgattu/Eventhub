package com.Eventhub.controller;

import com.Eventhub.dao.BookingCouponDAO;
import com.Eventhub.model.BookingCoupon;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/api/booking-coupons/*")
public class BookingCouponServlet extends JsonCrudServlet<BookingCoupon> {

    private static final BookingCouponDAO DAO = new BookingCouponDAO();

    public BookingCouponServlet() {
        super(BookingCoupon.class,
                (request) -> request.getParameter("bookingId") == null
                        ? DAO.getAllBookingCoupons()
                        : DAO.getByBookingId(requiredParameter(request, "bookingId")),
                (request, id) -> DAO.getBookingCouponById(id),
                (request, coupon) -> DAO.createBookingCoupon(coupon),
                null,
                (request, id) -> DAO.deleteBookingCoupon(id),
                BookingCoupon::setId,
                Policies.adminOnly());
    }
}
