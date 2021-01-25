package com.example.spring.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findByPersonName(String name);

    @Transactional
    @Procedure("transfer")
    void transfer(Integer source, Integer target, BigDecimal amount);
}
