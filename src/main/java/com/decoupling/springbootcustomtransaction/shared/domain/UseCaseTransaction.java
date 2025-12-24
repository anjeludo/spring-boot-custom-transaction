package com.decoupling.springbootcustomtransaction.shared.domain;

import java.lang.annotation.*;


@Documented
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UseCaseTransaction {
    TransactionPropagation propagation() default TransactionPropagation.REQUIRED;
    boolean readOnly() default false;
    int timeout() default -1;
    Class<? extends Throwable>[] rollbackFor() default {RuntimeException.class};

    public enum TransactionPropagation {
        REQUIRED, REQUIRES_NEW, MANDATORY, SUPPORTS, NOT_SUPPORTED, NEVER, NESTED
    }
}
