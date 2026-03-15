package com.example.ccledger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // 追加
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ccledger.domain.Transaction;

public interface TransactionRepository
    extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> { // ここが重要

    interface MonthlySummary {
        String getBillingMonth();
        Long getTotalAmount();
        Long getUnpaidAmount();
    }

    @Query(value = """
        SELECT
            billing_month AS billingMonth,
            SUM(amount) AS totalAmount,
            SUM(CASE WHEN paid = FALSE THEN amount ELSE 0 END) AS unpaidAmount
        FROM transactions
        WHERE billing_month BETWEEN :startMonth AND :endMonth
        GROUP BY billing_month
        ORDER BY billing_month
        """, nativeQuery = true)
    List<MonthlySummary> findMonthlySummary(
        @Param("startMonth") String startMonth,
        @Param("endMonth") String endMonth
    );
}
