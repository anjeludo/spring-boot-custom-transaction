package com.decoupling.springbootcustomtransaction.user.infra.persistence;

import com.decoupling.springbootcustomtransaction.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaUserRepository extends JpaRepository<User, Long> {
}