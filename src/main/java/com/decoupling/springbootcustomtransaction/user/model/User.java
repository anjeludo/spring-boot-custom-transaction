package com.decoupling.springbootcustomtransaction.user.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@Access(AccessType.FIELD)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;

    private User(String username, String email)  {
        this.username = username;
        this.email = email;
    }

    public static User create(String name, String email) {
        return new User(name, email);
    }
}
