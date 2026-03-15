package com.example.ccledger.web;

public class TransactionSearchForm {
  private String billingMonth;   // yyyy-MM（空なら全部）
  private Long cardId;           // nullなら全部
  private Boolean paid;          // nullなら全部
  private String keyword;        // itemName 部分一致

  public String getBillingMonth() { return billingMonth; }
  public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }

  public Long getCardId() { return cardId; }
  public void setCardId(Long cardId) { this.cardId = cardId; }

  public Boolean getPaid() { return paid; }
  public void setPaid(Boolean paid) { this.paid = paid; }

  public String getKeyword() { return keyword; }
  public void setKeyword(String keyword) { this.keyword = keyword; }
}
