package com.example.ccledger.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.ccledger.domain.Card;
import com.example.ccledger.repository.CardRepository;

@Component
public class DataLoader implements CommandLineRunner {

  private final CardRepository cardRepo;

  public DataLoader(CardRepository cardRepo) {
    this.cardRepo = cardRepo;
  }

  @Override
  public void run(String... args) {
    if (cardRepo.count() == 0) {
      cardRepo.saveAll(List.of(
        new Card("楽天カード"),
        new Card("三井住友カード"),
        new Card("JCB"),
        new Card("AMEX")
      ));
    }
  }
}
