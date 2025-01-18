package io.github.msj.swiftbank.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    void shouldRedirectToAdminDashboardWhenUserIsAdmin() {
        Authentication authentication = mock(Authentication.class);
        GrantedAuthority adminAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        when(authentication.getAuthorities()).thenReturn((Collection) Collections.singletonList(adminAuthority));

        String redirectUrl = dashboardController.redirectToDashboard(authentication);

        assertEquals("redirect:/admin/dashboard", redirectUrl);
        verify(authentication, times(1)).getAuthorities();
    }

    @Test
    void shouldRedirectToUserDashboardWhenUserIsNotAdmin() {
        Authentication authentication = mock(Authentication.class);
        GrantedAuthority userAuthority = mock(GrantedAuthority.class);
        when(userAuthority.getAuthority()).thenReturn("ROLE_USER");
        when(authentication.getAuthorities()).thenReturn((Collection) Collections.singletonList(userAuthority));

        String redirectUrl = dashboardController.redirectToDashboard(authentication);

        assertEquals("redirect:/user/dashboard", redirectUrl);
        verify(authentication, times(1)).getAuthorities();
    }

    @Test
    void shouldRedirectToUserDashboardWhenNoAdminAuthority() {
        Authentication authentication = mock(Authentication.class);
        GrantedAuthority userAuthority1 = mock(GrantedAuthority.class);
        GrantedAuthority userAuthority2 = mock(GrantedAuthority.class);
        when(userAuthority1.getAuthority()).thenReturn("ROLE_USER");
        when(userAuthority2.getAuthority()).thenReturn("ROLE_MANAGER");
        when(authentication.getAuthorities()).thenReturn((Collection) Arrays.asList(userAuthority1, userAuthority2));

        String redirectUrl = dashboardController.redirectToDashboard(authentication);

        assertEquals("redirect:/user/dashboard", redirectUrl);
        verify(authentication, times(1)).getAuthorities();
    }
}
