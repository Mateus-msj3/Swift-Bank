package io.github.msj.swiftbank.controller;

import io.github.msj.swiftbank.service.AccountService;
import io.github.msj.swiftbank.service.UserService;
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

    private final UserService userService;

    public AdminAccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
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
