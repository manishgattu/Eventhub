package com.Eventhub.controller;

import com.Eventhub.dao.CouponDAO;
import com.Eventhub.model.Coupon;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/api/coupons/*")
public class CouponServlet extends JsonCrudServlet<Coupon> {

    private static final CouponDAO DAO = new CouponDAO();

    public CouponServlet() {
        super(Coupon.class,
                CouponServlet::list,
                (request, id) -> DAO.getCouponById(id),
                (request, coupon) -> DAO.createCoupon(coupon),
                (request, coupon, id) -> {
                    coupon.setId(id);
                    return DAO.updateCoupon(coupon);
                },
                (request, id) -> DAO.deleteCoupon(id),
                Coupon::setId,
                Policies.readForUsersWriteForAdmins());
    }

    private static java.util.List<Coupon> list(
            jakarta.servlet.http.HttpServletRequest request) throws java.sql.SQLException {
        String code = request.getParameter("code");
        if (code != null) {
            Coupon coupon = DAO.getCouponByCode(code.trim());
            if (coupon == null || (!SessionAuth.isAdmin(request)
                    && !"ACTIVE".equals(coupon.getStatus()))) {
                return java.util.List.of();
            }
            return java.util.List.of(coupon);
        }
        return SessionAuth.isAdmin(request) ? DAO.getAllCoupons() : DAO.getActiveCoupons();
    }
}
