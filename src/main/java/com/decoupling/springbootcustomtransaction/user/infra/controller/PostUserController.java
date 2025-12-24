package com.decoupling.springbootcustomtransaction.user.infra.controller;

import com.decoupling.springbootcustomtransaction.user.application.UserRegistrar;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostUserController {

    private final UserRegistrar userRegistrar;

    @PostMapping("/users")
    public ResponseEntity<Void> createUser(@RequestBody RequestUser requestUser) {
        userRegistrar.execute(requestUser.username(), requestUser.email());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
