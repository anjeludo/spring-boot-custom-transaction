package com.decoupling.springbootcustomtransaction.shared.infra.transactions;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

@Configuration
@EnableTransactionManagement
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class TransactionConfig {

    @Bean
    public TransactionAttributeSource transactionAttributeSource() {
        return new UseCaseTransactionAttributeSource();
    }

    @Bean
    public TransactionInterceptor transactionInterceptor(
            PlatformTransactionManager txManager,
            TransactionAttributeSource txSource) {
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(txManager);
        interceptor.setTransactionAttributeSource(txSource);
        return interceptor;
    }

    @Bean
    public BeanFactoryTransactionAttributeSourceAdvisor advisor(
            TransactionInterceptor txInterceptor) {
        BeanFactoryTransactionAttributeSourceAdvisor advisor =
                new BeanFactoryTransactionAttributeSourceAdvisor();
        advisor.setAdvice(txInterceptor);
        advisor.setTransactionAttributeSource(transactionAttributeSource());
        return advisor;
    }
}