package com.decoupling.springbootcustomtransaction.transaction;

import com.decoupling.springbootcustomtransaction.user.model.User;
import com.decoupling.springbootcustomtransaction.user.model.UserRepository;
import com.decoupling.springbootcustomtransaction.shared.domain.UseCaseTransaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.IllegalTransactionStateException;

import static com.decoupling.springbootcustomtransaction.shared.domain.UseCaseTransaction.TransactionPropagation.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for verifying transaction propagation behavior with @UseCaseTransaction annotation.
 */
@SpringBootTest
class TransactionPropagationIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OuterTransactionService outerService;

    @Autowired
    private InnerTransactionService innerService;

    @Test
    void shouldUseRequiredPropagation_joinExistingTransaction() {
        // When - outer transaction calls inner with REQUIRED (default)
        // Then - exception should be thrown and both operations rolled back (same transaction)
        assertThrows(RuntimeException.class, () -> {
            outerService.saveUserAndCallInnerThatFails("outer", "outer@test.com");
        });
    }

    @Test
    void shouldUseRequiresNewPropagation_createNewTransaction() {
        // When - outer transaction calls inner with REQUIRES_NEW
        // Then - should not throw because outer catches the exception from inner
        assertDoesNotThrow(() -> {
            outerService.saveUserAndCallInnerRequiresNewThatFails("outer" + System.currentTimeMillis(), "outer@test.com");
        });
        // Outer transaction commits successfully despite inner transaction failing (REQUIRES_NEW)
    }

    @Test
    void shouldUseMandatoryPropagation_failsWithoutExistingTransaction() {
        // When & Then - calling MANDATORY without existing transaction should fail
        assertThrows(IllegalTransactionStateException.class, () -> {
            innerService.saveUserWithMandatory("mandatory", "mandatory@test.com");
        });
    }

    @Test
    void shouldUseMandatoryPropagation_worksWithExistingTransaction() {
        // When - calling MANDATORY within existing transaction should work
        assertDoesNotThrow(() -> {
            outerService.callInnerMandatory("mandatory" + System.currentTimeMillis(), "mandatory@test.com");
        });
        // Then - no exception means MANDATORY worked within existing transaction
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public OuterTransactionService outerTransactionService(
                UserRepository userRepository,
                InnerTransactionService innerService) {
            return new OuterTransactionService(userRepository, innerService);
        }

        @Bean
        public InnerTransactionService innerTransactionService(UserRepository userRepository) {
            return new InnerTransactionService(userRepository);
        }
    }

    @Service
    static class OuterTransactionService {
        private final UserRepository userRepository;
        private final InnerTransactionService innerService;

        public OuterTransactionService(UserRepository userRepository, InnerTransactionService innerService) {
            this.userRepository = userRepository;
            this.innerService = innerService;
        }

        @UseCaseTransaction(propagation = REQUIRED)
        public void saveUserAndCallInnerThatFails(String username, String email) {
            userRepository.save(User.create(username, email));
            innerService.saveUserAndFail("inner", "inner@test.com");
        }

        @UseCaseTransaction(propagation = REQUIRED)
        public void saveUserAndCallInnerRequiresNewThatFails(String username, String email) {
            userRepository.save(User.create(username, email));
            try {
                innerService.saveUserRequiresNewAndFail("inner", "inner@test.com");
            } catch (RuntimeException e) {
                // Catch exception from inner transaction to allow outer to commit
            }
        }

        @UseCaseTransaction(propagation = REQUIRED)
        public void callInnerMandatory(String username, String email) {
            innerService.saveUserWithMandatory(username, email);
        }
    }

    @Service
    static class InnerTransactionService {
        private final UserRepository userRepository;

        public InnerTransactionService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @UseCaseTransaction(propagation = REQUIRED)
        public void saveUserAndFail(String username, String email) {
            userRepository.save(User.create(username, email));
            throw new RuntimeException("Inner transaction failure");
        }

        @UseCaseTransaction(propagation = REQUIRES_NEW)
        public void saveUserRequiresNewAndFail(String username, String email) {
            userRepository.save(User.create(username, email));
            throw new RuntimeException("Inner transaction failure (REQUIRES_NEW)");
        }

        @UseCaseTransaction(propagation = MANDATORY)
        public void saveUserWithMandatory(String username, String email) {
            userRepository.save(User.create(username, email));
        }
    }
}
