package com.decoupling.springbootcustomtransaction.user.infra.persistence;


import com.decoupling.springbootcustomtransaction.user.model.User;
import com.decoupling.springbootcustomtransaction.user.model.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class H2UserRepository implements UserRepository {

    private final JpaUserRepository jpaUserRepository;

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }
}
