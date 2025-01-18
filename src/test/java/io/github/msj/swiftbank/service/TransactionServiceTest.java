package io.github.msj.swiftbank.service;

import io.github.msj.swiftbank.entity.Transaction;
import io.github.msj.swiftbank.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldSaveTransactionSuccessfully() {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAmount(new BigDecimal("100"));
        transaction.setTransactionType("CREDIT");

        when(transactionRepository.save(transaction)).thenReturn(transaction);

        Transaction savedTransaction = transactionService.save(transaction);

        assertNotNull(savedTransaction);
        assertEquals(1L, savedTransaction.getId());
        assertEquals(new BigDecimal("100"), savedTransaction.getAmount());
        assertEquals("CREDIT", savedTransaction.getTransactionType());
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void shouldGetTransactionsByAccountSuccessfully() {
        Transaction transaction1 = new Transaction();
        transaction1.setId(1L);
        transaction1.setAmount(new BigDecimal("100"));
        transaction1.setTransactionType("CREDIT");

        Transaction transaction2 = new Transaction();
        transaction2.setId(2L);
        transaction2.setAmount(new BigDecimal("200"));
        transaction2.setTransactionType("DEBIT");

        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);

        when(transactionRepository.findByAccountId(1L)).thenReturn(transactions);

        List<Transaction> result = transactionService.getTransactionsByAccount(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        verify(transactionRepository, times(1)).findByAccountId(1L);
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionsFound() {
        when(transactionRepository.findByAccountId(1L)).thenReturn(Collections.emptyList());

        List<Transaction> result = transactionService.getTransactionsByAccount(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository, times(1)).findByAccountId(1L);
    }
}
