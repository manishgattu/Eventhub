package com.Eventhub.controller;

import com.Eventhub.dao.ReviewDAO;
import com.Eventhub.model.Review;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/reviews/*")
public class ReviewServlet extends JsonCrudServlet<Review> {

    private static final ReviewDAO DAO = new ReviewDAO();

    public ReviewServlet() {
        super(Review.class,
                ReviewServlet::list,
                (request, id) -> DAO.getReviewById(id),
                (request, review) -> {
                    if (!SessionAuth.isAdmin(request)) {
                        review.setUserId(SessionAuth.userId(request));
                    }
                    return DAO.createReview(review);
                },
                (request, review, id) -> {
                    review.setId(id);
                    if (!SessionAuth.isAdmin(request)) {
                        review.setUserId(SessionAuth.userId(request));
                    }
                    return DAO.updateReview(review);
                },
                (request, id) -> DAO.deleteReview(id),
                Review::setId,
                Policies.customerOwned(Review::getUserId, true, true));
    }

    private static List<Review> list(HttpServletRequest request) throws SQLException {
        String serviceId = request.getParameter("serviceId");
        String userId = request.getParameter("userId");
        if (serviceId != null) {
            return DAO.getReviewsByServiceId(requiredParameter(request, "serviceId"));
        }
        if (userId != null && SessionAuth.isAdmin(request)) {
            return DAO.getReviewsByUserId(requiredParameter(request, "userId"));
        }
        if (SessionAuth.isAdmin(request)) {
            return DAO.getAllReviews();
        }
        return DAO.getReviewsByUserId(SessionAuth.userId(request));
    }
}
