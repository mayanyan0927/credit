package com.example.ccledger.repository;

import org.springframework.data.jpa.domain.Specification;

import com.example.ccledger.domain.Transaction;

public class TransactionSpecification {//SQLを作成

  public static Specification<Transaction> billingMonthEquals(String billingMonth) {
    return (root, query, cb) -> {
      if (billingMonth == null || billingMonth.isBlank()) return cb.conjunction();
      return cb.equal(root.get("billingMonth"), billingMonth);
    };
  }

  public static Specification<Transaction> cardEquals(Long cardId) {
    return (root, query, cb) -> {
      if (cardId == null) return cb.conjunction();
      return cb.equal(root.get("card").get("id"), cardId);
    };
  }

  public static Specification<Transaction> paidEquals(Boolean paid) {
    return (root, query, cb) -> {
      if (paid == null) return cb.conjunction();
      return cb.equal(root.get("paid"), paid);
    };
  }

  public static Specification<Transaction> itemNameLike(String keyword) {
    return (root, query, cb) -> {
      if (keyword == null || keyword.isBlank()) return cb.conjunction();
      return cb.like(root.get("itemName"), "%" + keyword + "%");
    };
  }
}
