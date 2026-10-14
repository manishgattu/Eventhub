package com.Eventhub.controller;

import com.Eventhub.dao.PaymentDAO;
import com.Eventhub.model.Payment;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/api/payments/*")
public class PaymentServlet extends JsonCrudServlet<Payment> {

    private static final PaymentDAO DAO = new PaymentDAO();

    public PaymentServlet() {
        super(Payment.class,
                (request) -> request.getParameter("bookingId") == null
                        ? DAO.getAllPayments()
                        : DAO.getPaymentsByBookingId(
                                requiredParameter(request, "bookingId")),
                (request, id) -> DAO.getPaymentById(id),
                (request, payment) -> DAO.createPayment(payment),
                (request, payment, id) -> {
                    payment.setId(id);
                    return DAO.updatePayment(payment);
                },
                (request, id) -> DAO.deletePayment(id),
                Payment::setId,
                Policies.adminOnly());
    }
}
