package io.github.msj.swiftbank.service;

import io.github.msj.swiftbank.repository.UserRepository;
import io.github.msj.swiftbank.entity.Role;
import io.github.msj.swiftbank.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setPassword("password123");

        Role role1 = new Role();
        role1.setName("USER");

        Role role2 = new Role();
        role2.setName("ADMIN");

        mockUser.setRoles(new HashSet<>(Arrays.asList(role1, role2)));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(mockUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())
                .containsAll(Arrays.asList("ROLE_USER", "ROLE_ADMIN")));

        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("unknownuser")
        );
        assertEquals("User not found: unknownuser", exception.getMessage());

        verify(userRepository, times(1)).findByUsername("unknownuser");
    }
}