package io.github.msj.swiftbank.repository;

import io.github.msj.swiftbank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
