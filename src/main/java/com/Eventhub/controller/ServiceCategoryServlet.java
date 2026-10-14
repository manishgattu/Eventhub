package com.Eventhub.controller;

import com.Eventhub.dao.ServiceCategoryDAO;
import com.Eventhub.model.ServiceCategory;
import jakarta.servlet.annotation.WebServlet;

@WebServlet("/api/service-categories/*")
public class ServiceCategoryServlet extends JsonCrudServlet<ServiceCategory> {

    private static final ServiceCategoryDAO DAO = new ServiceCategoryDAO();

    public ServiceCategoryServlet() {
        super(ServiceCategory.class,
                (request) -> DAO.getAllCategories(),
                (request, id) -> DAO.getCategoryById(id),
                (request, category) -> DAO.createCategory(category),
                (request, category, id) -> {
                    category.setId(id);
                    return DAO.updateCategory(category);
                },
                (request, id) -> DAO.deleteCategory(id),
                ServiceCategory::setId,
                Policies.readForUsersWriteForAdmins());
    }
}
