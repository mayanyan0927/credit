package com.example.ccledger.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ccledger.domain.Card;

public interface CardRepository extends JpaRepository<Card, Long> {
  Optional<Card> findByName(String name);
}
