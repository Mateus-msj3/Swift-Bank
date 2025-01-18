package io.github.msj.swiftbank.controller;

import io.github.msj.swiftbank.entity.User;
import io.github.msj.swiftbank.service.AccountService;
import io.github.msj.swiftbank.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/user")
public class UserTransactionController {

    private final AccountService accountService;

    private final UserService userService;

    public UserTransactionController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        BigDecimal totalBalance = accountService.calculateTotalBalanceByUser(user.getId());
        model.addAttribute("totalBalance", totalBalance != null ? totalBalance : BigDecimal.ZERO);
        return "user-dashboard";
    }

    @GetMapping("/accounts/list")
    public String listUserAccounts(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("accounts", accountService.getAccountsByUser(user.getId()));
        return "user-account-list";
    }

    @GetMapping("/accounts/credit")
    public String showCreditForm(Model model, Authentication authentication) {
        addUserAccountsToModel(model, authentication);
        return "credit-account";
    }

    @PostMapping("/accounts/credit")
    public String creditAccount(@RequestParam Long accountId,
                                @RequestParam BigDecimal amount,
                                Model model,
                                Authentication authentication) {
        try {
            accountService.creditAccount(accountId, amount);
            model.addAttribute("successMessage", "Valor creditado com sucesso!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        addUserAccountsToModel(model, authentication);
        return "credit-account";
    }

    @GetMapping("/accounts/debit")
    public String showDebitForm(Model model, Authentication authentication) {
        addUserAccountsToModel(model, authentication);
        return "debit-account";
    }

    @PostMapping("/accounts/debit")
    public String debitAccount(@RequestParam Long accountId,
                               @RequestParam BigDecimal amount,
                               Model model,
                               Authentication authentication) {
        try {
            accountService.debitAccount(accountId, amount);
            model.addAttribute("successMessage", "Valor debitado com sucesso!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        addUserAccountsToModel(model, authentication);
        return "debit-account";
    }

    @GetMapping("/accounts/transfer")
    public String showTransferForm(Model model, Authentication authentication) {
        populateTransferForm(model, authentication);
        return "transfer-account";
    }

    @PostMapping("/accounts/transfer")
    public String transferBetweenAccounts(@RequestParam Long sourceAccountId,
                                          @RequestParam Long targetAccountId,
                                          @RequestParam BigDecimal amount,
                                          Model model,
                                          Authentication authentication) {
        try {
            accountService.transferBetweenAccounts(sourceAccountId, targetAccountId, amount);
            model.addAttribute("successMessage", "Transferência realizada com sucesso!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        populateTransferForm(model, authentication);

        return "transfer-account";
    }


    private void populateTransferForm(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("accounts", accountService.getAccountsByUser(user.getId()));
        model.addAttribute("targetAccounts", accountService.getAccountsExcludingUser(user.getId()));
    }

    private void addUserAccountsToModel(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("accounts", accountService.getAccountsByUser(user.getId()));
    }
}
