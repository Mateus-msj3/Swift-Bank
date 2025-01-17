package io.github.msj.swiftbank.controller;

import io.github.msj.swiftbank.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminAccountController {

    private final AccountService accountService;

    public AdminAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/accounts/create")
    public String showCreateAccountForm() {
        return "create-account";
    }

    @PostMapping("/accounts/create")
    public String createAccount(@RequestParam String ownerName, @RequestParam BigDecimal initialBalance, Model model) {
        try {
            accountService.createAccount(ownerName, initialBalance);
            model.addAttribute("successMessage", "Conta criada com sucesso!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "create-account";
    }
}
