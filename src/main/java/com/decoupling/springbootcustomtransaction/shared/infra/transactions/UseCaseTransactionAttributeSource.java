package com.decoupling.springbootcustomtransaction.shared.infra.transactions;

import com.decoupling.springbootcustomtransaction.shared.domain.UseCaseTransaction;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class UseCaseTransactionAttributeSource implements TransactionAttributeSource {

    

    @Override
    public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
        UseCaseTransaction customTransactionAnnotation = method.getAnnotation(UseCaseTransaction.class);
        if (customTransactionAnnotation == null && targetClass != null) {
            customTransactionAnnotation = targetClass.getAnnotation(UseCaseTransaction.class);
        }

        if (customTransactionAnnotation != null) {
            RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();

            // Propagation behavior
            rbta.setPropagationBehaviorName("PROPAGATION_" + customTransactionAnnotation.propagation().name());
            rbta.setReadOnly(customTransactionAnnotation.readOnly());
            rbta.setTimeout(customTransactionAnnotation.timeout());

            // Rollback rules
            rbta.setRollbackRules(
                    Arrays.stream(customTransactionAnnotation.rollbackFor())
                            .map(RollbackRuleAttribute::new)
                            .collect(Collectors.toList())
            );

            return rbta;
        }
        return null;
    }
}