package com.decoupling.springbootcustomtransaction.user.application;

import com.decoupling.springbootcustomtransaction.user.model.User;
import com.decoupling.springbootcustomtransaction.user.model.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRegistrar {

    private  final UserRepository userRepository;

    @Transactional
    public void execute(String username, String email) {
        userRepository.save(User.create(username, email));
    }
}
