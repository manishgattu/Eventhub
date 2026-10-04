 package com.Eventhub.dao;

import com.Eventhub.model.User;

public class UserDAOTest {

    public static void main(String[] args) {

        UserDAO userDAO = new UserDAO();

        User user = userDAO.getUserById(1L);

        if (user != null) {
            System.out.println("User found!");
            System.out.println(user);
        } else {
            System.out.println("User not found.");
        }
    }
}