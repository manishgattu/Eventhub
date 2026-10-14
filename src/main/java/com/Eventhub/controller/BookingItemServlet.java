package com.Eventhub.controller;

import com.Eventhub.dao.BookingItemDAO;
import com.Eventhub.model.BookingItem;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/api/booking-items/*")
public class BookingItemServlet extends JsonCrudServlet<BookingItem> {

    private static final BookingItemDAO DAO = new BookingItemDAO();

    public BookingItemServlet() {
        super(BookingItem.class,
                (request) -> DAO.getItemsByBookingId(
                        requiredParameter(request, "bookingId")),
                (request, id) -> DAO.getBookingItemById(id),
                (request, item) -> DAO.createBookingItem(item),
                (request, item, id) -> {
                    item.setId(id);
                    return DAO.updateBookingItem(item);
                },
                (request, id) -> DAO.deleteBookingItem(id),
                BookingItem::setId,
                Policies.adminOnly());
    }
}
