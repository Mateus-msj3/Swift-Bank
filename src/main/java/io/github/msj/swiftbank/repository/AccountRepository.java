package io.github.msj.swiftbank.repository;

import io.github.msj.swiftbank.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserId(Long userId);

    @Query("SELECT a FROM Account a WHERE a.user.id != :userId")
    List<Account> findAccountsExcludingUser(@Param("userId") Long userId);

}
