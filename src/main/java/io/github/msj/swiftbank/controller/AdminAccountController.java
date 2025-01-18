package io.github.msj.swiftbank.controller;

import io.github.msj.swiftbank.entity.Account;
import io.github.msj.swiftbank.entity.Transaction;
import io.github.msj.swiftbank.service.AccountService;
import io.github.msj.swiftbank.service.TransactionService;
import io.github.msj.swiftbank.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminAccountController {

    private final AccountService accountService;

    private final UserService userService;

    private final TransactionService transactionService;

    public AdminAccountController(AccountService accountService, UserService userService,
                                  TransactionService transactionService) {
        this.accountService = accountService;
        this.userService = userService;
        this.transactionService = transactionService;
    }

    @GetMapping("/accounts/list")
    public String listAllAccounts(Model model) {
        model.addAttribute("accounts", accountService.findAll());
        return "admin-account-list";
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("totalAccounts", accountService.countAccounts());
        model.addAttribute("totalBalance", accountService.calculateTotalBalance());
        return "admin-dashboard";
    }

    @GetMapping("/accounts/transactions/selection")
    public String showTransactionSelection(Model model) {
        List<Account> allAccounts = accountService.findAll();
        model.addAttribute("accounts", allAccounts);

        return "admin-transaction-selection";
    }

    @PostMapping("/transactions")
    public String listAdminTransactions(@RequestParam Long accountId, Model model) {
        Account account = accountService.findById(accountId);
        List<Transaction> transactions = transactionService.getTransactionsByAccount(accountId);
        model.addAttribute("transactions", transactions);
        model.addAttribute("selectedAccount", account);

        return "admin-transaction-list";
    }

    @GetMapping("/accounts/create")
    public String showCreateAccountForm(Model model) {
        model.addAttribute("users", userService.findAll());
        return "create-account";
    }

    @PostMapping("/accounts/create")
    public String createAccount(@RequestParam String ownerName,
                                @RequestParam BigDecimal initialBalance,
                                @RequestParam Long userId,
                                Model model) {
        try {
            accountService.createAccount(ownerName, initialBalance, userId);
            model.addAttribute("successMessage", "Conta criada com sucesso!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        model.addAttribute("users", userService.findAll());
        return "create-account";
    }

}
