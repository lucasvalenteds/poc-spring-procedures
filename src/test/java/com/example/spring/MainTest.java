package com.example.spring;

import com.example.spring.testing.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import java.sql.Types;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testFailingToUseSimpleJdbcCall() {
        int johnId = getAccountId("John Smith");
        int maryId = getAccountId("Mary Jane");
        double amountToTransfer = 500d;

        Throwable throwable = assertThrows(BadSqlGrammarException.class, () ->
            new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("transfer")
                .declareParameters(
                    new SqlParameter("source", Types.INTEGER),
                    new SqlParameter("target", Types.INTEGER),
                    new SqlParameter("amount", Types.DECIMAL)
                )
                .execute(Map.of(
                    "source", maryId,
                    "target", johnId,
                    "amount", amountToTransfer
                ))
        );

        // https://github.com/spring-projects/spring-framework/issues/26014
        // https://github.com/pgjdbc/pgjdbc/issues/1413
        assertTrue(throwable.getMessage().contains("To call a procedure, use CALL"));
    }

    private int getAccountId(String personName) {
        Integer accountId = jdbcTemplate.queryForObject(
            "select id from accounts where owner = (select id from people where name = ?)",
            Integer.class,
            personName
        );

        assertNotNull(accountId);

        return accountId;
    }
}
