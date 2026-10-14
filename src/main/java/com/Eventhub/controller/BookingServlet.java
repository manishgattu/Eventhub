package com.Eventhub.controller;

import com.Eventhub.dao.BookingDAO;
import com.Eventhub.model.Booking;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/bookings/*")
public class BookingServlet extends JsonCrudServlet<Booking> {

    private static final BookingDAO DAO = new BookingDAO();

    public BookingServlet() {
        super(Booking.class,
                BookingServlet::list,
                (request, id) -> DAO.getBookingById(id),
                (request, booking) -> {
                    if (!SessionAuth.isAdmin(request)) {
                        booking.setUserId(SessionAuth.userId(request));
                        booking.setStatus("PENDING");
                        booking.setTotalAmount(java.math.BigDecimal.ZERO);
                    }
                    return DAO.createBooking(booking);
                },
                (request, booking, id) -> {
                    booking.setId(id);
                    return DAO.updateBooking(booking);
                },
                (request, id) -> DAO.deleteBooking(id),
                Booking::setId,
                Policies.customerOwned(Booking::getUserId, false, false));
    }

    private static List<Booking> list(HttpServletRequest request)
            throws SQLException {
        if (SessionAuth.isAdmin(request)) {
            String userId = request.getParameter("userId");
            if (userId != null) {
                return DAO.getBookingsByUserId(requiredParameter(request, "userId"));
            }
            String eventId = request.getParameter("eventId");
            if (eventId != null) {
                return DAO.getBookingsByEventId(requiredParameter(request, "eventId"));
            }
            return DAO.getAllBookings();
        }
        String eventId = request.getParameter("eventId");
        if (eventId != null) {
            return DAO.getBookingsByEventId(requiredParameter(request, "eventId"));
        }
        return DAO.getBookingsByUserId(SessionAuth.userId(request));
    }
}
