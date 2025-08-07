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

    @PostMapping("/register")
    public void addNewUser(@RequestBody User user) {
        userService.addNewUser(user);
    }

    @PostMapping("/login")
    public void login(@RequestBody LoginRequest request ) {
        userService.login(request.getEmail(), request.getPassword());
    }

    @PutMapping(path = "{userId}")
    public void updateUser(@PathVariable("userId") Long userId,
                           @RequestBody UpdateUserRequest request) {
        userService.updateUser(userId, request);
    }

}
