package com.example.ccledger.web;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TransactionForm {

  @NotBlank
  private String itemName;

  @NotNull
  private Long cardId;

  @NotNull @Min(0)
  private Integer amount;

  // 当日をデフォルト
  @NotNull
  private LocalDate purchaseDate = LocalDate.now();

  @NotBlank
  private String billingMonth; // yyyy-MM

  private String paidMonth; // yyyy-MM（支払済みなら入れる）
  private boolean paid;

  // getters/setters
  public String getItemName() { return itemName; }
  public void setItemName(String itemName) { this.itemName = itemName; }

  public Long getCardId() { return cardId; }
  public void setCardId(Long cardId) { this.cardId = cardId; }

  public Integer getAmount() { return amount; }
  public void setAmount(Integer amount) { this.amount = amount; }

  public LocalDate getPurchaseDate() { return purchaseDate; }
  public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }

  public String getBillingMonth() { return billingMonth; }
  public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }

  public String getPaidMonth() { return paidMonth; }
  public void setPaidMonth(String paidMonth) { this.paidMonth = paidMonth; }

  public boolean isPaid() { return paid; }
  public void setPaid(boolean paid) { this.paid = paid; }
}
