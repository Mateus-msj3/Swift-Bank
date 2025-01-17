package io.github.msj.swiftbank.repository;

import io.github.msj.swiftbank.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
