package com.example.spring;

import com.example.spring.account.Account;
import com.example.spring.account.AccountRepository;
import org.hibernate.testing.transaction.TransactionUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @Tag("JDBC")
    public void testPreparedStatementWithParameters() {
        int johnId = getAccountId("John Smith");
        int maryId = getAccountId("Mary Jane");
        double johnInitialBalance = getAccountBalance(johnId);
        double maryInitialBalance = getAccountBalance(maryId);
        double amountToTransfer = 500d;

        jdbcTemplate.call(connection -> {
            CallableStatement statement = connection.prepareCall("CALL transfer(?, ?, ?)");
            statement.setInt(1, maryId);
            statement.setInt(2, johnId);
            statement.setBigDecimal(3, BigDecimal.valueOf(amountToTransfer));
            return statement;
        }, List.of(
            new SqlParameter("source", Types.INTEGER),
            new SqlParameter("target", Types.INTEGER),
            new SqlParameter("amount", Types.DECIMAL)
        ));

        double johnFinalBalance = getAccountBalance(johnId);
        double maryFinalBalance = getAccountBalance(maryId);

        assertEquals(johnFinalBalance, johnInitialBalance + amountToTransfer);
        assertEquals(maryFinalBalance, maryInitialBalance - amountToTransfer);
    }

    @Test
    @Tag("JDBC")
    public void testFailingToUsePreparedStatementWithNamedParameters() {
        int johnId = getAccountId("John Smith");
        int maryId = getAccountId("Mary Jane");
        double amountToTransfer = 500d;

        Throwable throwable = assertThrows(InvalidDataAccessApiUsageException.class, () ->
            jdbcTemplate.call(connection -> {
                CallableStatement statement = connection.prepareCall("CALL transfer(?, ?, ?)");
                statement.setInt("source", maryId);
                statement.setInt("target", johnId);
                statement.setBigDecimal("amount", BigDecimal.valueOf(amountToTransfer));
                return statement;
            }, List.of(
                new SqlParameter("source", Types.INTEGER),
                new SqlParameter("target", Types.INTEGER),
                new SqlParameter("amount", Types.DECIMAL)
            )));

        // https://github.com/pgjdbc/pgjdbc/blob/master/pgjdbc/src/main/java/org/postgresql/jdbc/PgCallableStatement.java#L710-L712
        assertTrue(
            throwable.getMessage()
                .contains("Method org.postgresql.jdbc.PgCallableStatement.setInt(String,int) is not yet implemented.")
        );
    }

    @Test
    @Tag("JDBC")
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

    @Test
    @Tag("JPA")
    public void testFailingToUseProcedureAnnotation() {
        Account john = accountRepository.findByPersonName("John Smith").orElseThrow();
        Account mary = accountRepository.findByPersonName("Mary Jane").orElseThrow();
        BigDecimal amountToTransfer = BigDecimal.valueOf(500d);

        Throwable throwable = assertThrows(InvalidDataAccessResourceUsageException.class, () ->
            accountRepository.transfer(mary.getId(), john.getId(), amountToTransfer)
        );

        // https://github.com/spring-projects/spring-framework/issues/26014
        // https://github.com/pgjdbc/pgjdbc/issues/1413
        assertTrue(getLatestExceptionThrown(throwable).getMessage().contains("To call a procedure, use CALL"));
    }

    @Test
    @Tag("JPA")
    public void testFailingToUseEntityManagerCreateStoredProcedureQuery() {
        Account john = accountRepository.findByPersonName("John Smith").orElseThrow();
        Account mary = accountRepository.findByPersonName("Mary Jane").orElseThrow();
        BigDecimal amountToTransfer = BigDecimal.valueOf(500d);

        Throwable throwable = assertThrows(PersistenceException.class, () ->
            entityManager.createStoredProcedureQuery("transfer")
                .registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(2, Integer.class, ParameterMode.IN)
                .registerStoredProcedureParameter(3, BigDecimal.class, ParameterMode.IN)
                .setParameter(1, mary.getId())
                .setParameter(2, john.getId())
                .setParameter(3, amountToTransfer)
                .execute()
        );

        assertTrue(getLatestExceptionThrown(throwable).getMessage().contains("To call a procedure, use CALL"));
    }

    @Test
    @Tag("JPA")
    public void testFailingToUseEntityManagerCreateNativeQuery() {
        Account john = accountRepository.findByPersonName("John Smith").orElseThrow();
        Account mary = accountRepository.findByPersonName("Mary Jane").orElseThrow();
        BigDecimal amountToTransfer = BigDecimal.valueOf(500d);


        Throwable throwable = assertThrows(PersistenceException.class, () ->
            TransactionUtil.doInJPA(() -> entityManager.getEntityManagerFactory(), em -> {
                em.createNativeQuery("call transfer(?, ?, ?)")
                    .setParameter(1, mary.getId())
                    .setParameter(2, john.getId())
                    .setParameter(3, amountToTransfer)
                    .executeUpdate();
            })
        );

        assertTrue(getLatestExceptionThrown(throwable).getMessage().contains("ERROR: invalid transaction termination"));
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

    private double getAccountBalance(int accountId) {
        Double balance = jdbcTemplate.queryForObject(
            "select balance from accounts where id = ?",
            Double.class,
            accountId
        );

        assertNotNull(balance);

        return balance;
    }

    private Throwable getLatestExceptionThrown(Throwable throwable) {
        Throwable cause = throwable.getCause();

        if (cause == null) {
            return throwable;
        }

        return getLatestExceptionThrown(cause);
    }

}
