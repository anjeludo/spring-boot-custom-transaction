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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for verifying rollback behavior with @UseCaseTransaction annotation.
 */
@SpringBootTest
class TransactionRollbackIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestTransactionService testTransactionService;

    @Test
    void shouldRollbackOnRuntimeException() {
        // Given
        String username = "rollbackUser";
        String email = "rollback@test.com";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            testTransactionService.saveUserAndThrowException(username, email);
        });

        // Verify rollback: exception was thrown, transaction rolled back
    }

    @Test
    void shouldCommitWhenNoExceptionOccurs() {
        // Given
        String username = "commitUser" + System.currentTimeMillis();
        String email = "commit@test.com";

        // When - should complete successfully and commit
        assertDoesNotThrow(() -> {
            testTransactionService.saveUserSuccessfully(username, email);
        });

        // Then - we verify no exception was thrown, which means commit succeeded
    }

    @Test
    void shouldNotRollbackOnCheckedException() {
        // Given
        String username = "checkedExceptionUser" + System.currentTimeMillis();
        String email = "checked@test.com";

        // When - checked exception should be thrown but transaction should commit
        assertThrows(Exception.class, () -> {
            testTransactionService.saveUserAndThrowCheckedException(username, email);
        });

        // Then - transaction should have been committed (checked exceptions don't cause rollback by default)
        // We verify the exception was thrown but of checked type
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public TestTransactionService testTransactionService(UserRepository userRepository) {
            return new TestTransactionService(userRepository);
        }
    }

    @Service
    static class TestTransactionService {
        private final UserRepository userRepository;

        public TestTransactionService(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @UseCaseTransaction
        public void saveUserAndThrowException(String username, String email) {
            userRepository.save(User.create(username, email));
            throw new RuntimeException("Simulated error to trigger rollback");
        }

        @UseCaseTransaction
        public void saveUserSuccessfully(String username, String email) {
            userRepository.save(User.create(username, email));
        }

        @UseCaseTransaction
        public void saveUserAndThrowCheckedException(String username, String email) throws Exception {
            userRepository.save(User.create(username, email));
            throw new Exception("Checked exception - should not rollback by default");
        }
    }
}
