package com.example.ccledger.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "item_name", nullable = false)
  private String itemName;

  @ManyToOne(optional = false)
  @JoinColumn(name = "card_id", nullable = false)
  private Card card;

  @Column(name = "amount", nullable = false)
  private Integer amount;

  @Column(name = "purchase_date", nullable = false)
  private LocalDate purchaseDate;

  @Column(name = "billing_month", nullable = false) // yyyy-MM
  private String billingMonth;

  @Column(name = "paid_month") // yyyy-MM (null可)
  private String paidMonth;

  @Column(name = "paid", nullable = false)
  private boolean paid;

  public Long getId() { return id; }

  public String getItemName() { return itemName; }
  public void setItemName(String itemName) { this.itemName = itemName; }

  public Card getCard() { return card; }
  public void setCard(Card card) { this.card = card; }

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
