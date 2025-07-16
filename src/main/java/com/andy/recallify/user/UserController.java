package com.andy.recallify.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String getUsers() {
        return "Hello World";
    }

    @PostMapping
    public void addNewUser(@RequestBody User user) {
        userService.addNewUser(user);
    }

}
