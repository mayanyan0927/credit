package com.example.ccledger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity //DBと紐づく
@Table(name = "cards")
public class Card {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) //ID自動採番
  private Long id;

  @Column(nullable = false, unique = true) //nullなし　重複禁止
  private String name;

  public Card() {}
  public Card(String name) { this.name = name; }

  public Long getId() { return id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
}
