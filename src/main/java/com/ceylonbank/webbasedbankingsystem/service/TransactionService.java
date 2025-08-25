package com.ceylonbank.webbasedbankingsystem.service;

import com.ceylonbank.webbasedbankingsystem.entity.Transaction;
import com.ceylonbank.webbasedbankingsystem.exception.BusinessException;
import com.ceylonbank.webbasedbankingsystem.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private TransactionRepository transactionRepository;

    public void processTransaction(Integer accountId,
                                   Integer targetAccountId,
                                   String type,
                                   BigDecimal amount,
                                   String description,
                                   String referenceNumber,
                                   Integer cashierUserId) {
        SimpleJdbcCall call = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("ProcessTransaction")
                .withSchemaName("dbo")
                .declareParameters(
                        new SqlParameter("AccountID", Types.INTEGER),
                        new SqlParameter("TargetAccountID", Types.INTEGER),
                        new SqlParameter("Type", Types.VARCHAR),
                        new SqlParameter("Amount", Types.DECIMAL),
                        new SqlParameter("Description", Types.VARCHAR),
                        new SqlParameter("ReferenceNumber", Types.VARCHAR),
                        new SqlParameter("PerformedBy", Types.INTEGER)
                );

        MapSqlParameterSource in = new MapSqlParameterSource()
                .addValue("AccountID", accountId, Types.INTEGER)
                .addValue("TargetAccountID", targetAccountId, Types.INTEGER)
                .addValue("Type", type, Types.VARCHAR)
                .addValue("Amount", amount, Types.DECIMAL)
                .addValue("Description", description, Types.VARCHAR)
                .addValue("ReferenceNumber", referenceNumber, Types.VARCHAR)
                .addValue("PerformedBy", cashierUserId, Types.INTEGER);

        try {
            call.execute(in);
        } catch (Exception ex) {
            throw new BusinessException(ex.getMessage() != null ? ex.getMessage() : "Transaction failed", ex);
        }
    }
    
    public List<Transaction> getRecentTransactionsByUserId(Integer userId, int limit) {
        return transactionRepository.findRecentTransactionsByUserId(userId, limit);
    }
}


