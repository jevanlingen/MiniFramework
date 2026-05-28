package com.di.exampleapp.controller;

import com.di.annotations.RestController;
import com.di.annotations.http.GET;
import com.di.annotations.http.POST;
import com.di.annotations.http.PathVariable;
import com.di.annotations.http.RequestParam;
import com.di.annotations.http.RequestBody;
import com.di.architecture.EventBus;
import com.di.exampleapp.service.AuthenticationService;
import com.di.exampleapp.service.UserService;
import com.di.model.User;
import com.di.model.events.audit.Create;
import com.di.model.events.audit.Read;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final EventBus eventBus;

    public UserController(final UserService userService, final AuthenticationService authenticationService, final EventBus eventBus) {
        this.userService = userService;
        this.authenticationService = authenticationService;
        this.eventBus = eventBus;
    }

    @GET("/user")
    public List<User> getUsers(@RequestParam("search") String searchTerm) {
        eventBus.publish(new Read("Getting users with term: " + searchTerm));
        return userService.search(searchTerm);
    }

    @GET("/user/{id}")
    public User getUser(@PathVariable("id") int id) {
        eventBus.publish(new Read("Getting user with id: " + id));
        return userService.getUser(id);
    }

    @POST("/user")
    public User createUser(@RequestBody User newUser) {
        eventBus.publish(new Create("Creating a new user: " + newUser.name()));
        return userService.createUser(newUser.name());
    }
}
