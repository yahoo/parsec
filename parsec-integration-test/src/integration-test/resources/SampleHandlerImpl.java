package com.example;

import com.example.parsec_generated.User;
import com.example.parsec_generated.ResourceContext;
import com.example.parsec_generated.SampleHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * SampleHandlerImpl is interface implementation that implement SampleHandler interface.
 */
public class SampleHandlerImpl implements SampleHandler {

    @Override
    public User getUser(ResourceContext context, Integer id) {
        return new User().setName("dm4");
    }

    @Override
    public String postUser(ResourceContext context, User user) {
        return "Hello " + user.getName() + "!\n";
    }

    @Override
    public String putUser(ResourceContext context, Integer id, User user) {
        return "Yo " + user.getOccupation() + "!\n";
    }

    @Override
    public ResourceContext newResourceContext(HttpServletRequest request, HttpServletResponse response) {
        return new DefaultResourceContext(request, response);
    }
}