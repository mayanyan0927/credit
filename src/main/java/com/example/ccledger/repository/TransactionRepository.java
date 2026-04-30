package com.example.ccledger.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository; //save findall findById delete
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // findAll(Specification spec)
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ccledger.domain.Transaction;

public interface TransactionRepository
    extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> { 

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


interface PaidMonthlySummary {
	  String getPaidMonth();
	  Long getTotalAmount();
	}

	@Query(value = """
	    SELECT
	      paid_month AS paidMonth,
	      COALESCE(SUM(amount), 0) AS totalAmount
	    FROM transactions
	    WHERE paid = TRUE
	      AND paid_month BETWEEN :startMonth AND :endMonth
	    GROUP BY paid_month
	    ORDER BY paid_month
	    """, nativeQuery = true)
	List<PaidMonthlySummary> findPaidMonthlySummary(
	    @Param("startMonth") String startMonth,
	    @Param("endMonth") String endMonth
	);
}