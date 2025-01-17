package io.github.msj.swiftbank.controller;

import io.github.msj.swiftbank.entity.User;
import io.github.msj.swiftbank.service.AccountService;
import io.github.msj.swiftbank.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class UserTransactionController {

    private final AccountService accountService;

    private final UserService userService;

    public UserTransactionController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    @GetMapping("/user/accounts/credit")
    public String showCreditForm(Model model, Authentication authentication) {
        addUserAccountsToModel(model, authentication);
        return "credit-account";
    }

    @PostMapping("/user/accounts/credit")
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

    private void addUserAccountsToModel(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("accounts", accountService.getAccountsByUser(user.getId()));
    }
}
