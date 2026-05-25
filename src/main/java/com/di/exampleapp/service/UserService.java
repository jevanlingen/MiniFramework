package com.di.exampleapp.service;

import com.di.annotations.Service;
import com.di.architecture.EventBus;
import com.di.exampleapp.repository.UserRepository;
import com.di.model.User;
import com.di.model.events.UserCreated;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EventBus eventBus;

    public UserService(final UserRepository userRepository, EventBus eventBus) {
        this.userRepository = userRepository;
        this.eventBus = eventBus;
    }

    public List<User> search(String term) {
        var users = userRepository.findAll();

        if (term == null) {
            return users;
        }

        return users.stream()
                .filter(user -> user.name().toLowerCase().contains(term.toLowerCase()))
                .toList();
    }

    public User getUser(int id) {
        return userRepository.findById(id);
    }

    public User createUser(String name) {
        final var newUser = userRepository.save(new User(null, name));
        eventBus.publish(new UserCreated(newUser));

        return newUser;
    }
}
