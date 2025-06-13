package com.devvault.Controller;

import com.devvault.model.User;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private Map<Long, User> userMap = new HashMap<>();
    private long idCounter = 1;

    @PostMapping
    public User createUser(@RequestBody User user) {
        user.setId(idCounter++);
        userMap.put(user.getId(), user);
        return user;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return new ArrayList<>(userMap.values());
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userMap.getOrDefault(id, null);
    }
}
