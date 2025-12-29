package com.decoupling.springbootcustomtransaction.shared.infra.transactions;

import com.decoupling.springbootcustomtransaction.shared.domain.UseCaseTransaction;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UseCaseTransactionAttributeSource implements TransactionAttributeSource {

    private final Map<Object, TransactionAttribute> attributeCache = new ConcurrentHashMap<>(1024);



    @Override
    public TransactionAttribute getTransactionAttribute(Method method, Class<?> targetClass) {
        if (method.getDeclaringClass() == Object.class) {
            return null;
        }

        // Check cache first
        Object cacheKey = getCacheKey(method, targetClass);
        TransactionAttribute cached = this.attributeCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // Compute transaction attribute
        TransactionAttribute txAttr = computeTransactionAttribute(method, targetClass);

        // Cache the result (even if null to avoid repeated lookups)
        if (txAttr != null) {
            this.attributeCache.put(cacheKey, txAttr);
        }

        return txAttr;
    }

    private TransactionAttribute computeTransactionAttribute(Method method, Class<?> targetClass) {
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

    private Object getCacheKey(Method method, Class<?> targetClass) {
        return new MethodClassKey(method, targetClass);
    }

    /**
     * Cache key for method and target class combination.
     */
    private static final class MethodClassKey {
        private final Method method;
        private final Class<?> targetClass;

        public MethodClassKey(Method method, Class<?> targetClass) {
            this.method = method;
            this.targetClass = targetClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodClassKey that = (MethodClassKey) o;
            return Objects.equals(method, that.method) && Objects.equals(targetClass, that.targetClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(method, targetClass);
        }
    }
}