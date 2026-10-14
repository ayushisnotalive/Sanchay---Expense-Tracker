package com.camuel.sanchay.backend.service;

import com.camuel.sanchay.backend.dto.AuthDto.*;
import com.camuel.sanchay.backend.entity.Category;
import com.camuel.sanchay.backend.entity.Role;
import com.camuel.sanchay.backend.entity.User;
import com.camuel.sanchay.backend.entity.Wallet;
import com.camuel.sanchay.backend.exception.BadRequestException;
import com.camuel.sanchay.backend.exception.ResourceNotFoundException;
import com.camuel.sanchay.backend.repository.CategoryRepository;
import com.camuel.sanchay.backend.repository.UserRepository;
import com.camuel.sanchay.backend.repository.WalletRepository;
import com.camuel.sanchay.backend.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       WalletRepository walletRepository,
                       CategoryRepository categoryRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        User user = new User();
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : request.getUsername());
        user.getRoles().add(Role.ROLE_USER);

        User savedUser = userRepository.save(user);

        // Seed default Primary Wallet
        Wallet primaryWallet = new Wallet();
        primaryWallet.setUser(savedUser);
        primaryWallet.setName("Primary Account");
        primaryWallet.setBalance(0.0);
        primaryWallet.setCurrency("$");
        primaryWallet.setColorHex("#00F3FF");
        walletRepository.save(primaryWallet);

        // Seed default Categories
        List<Category> defaults = List.of(
            new Category(savedUser, "Food & Dining", "fastfood", false),
            new Category(savedUser, "Shopping", "shopping_cart", false),
            new Category(savedUser, "Transportation", "directions_car", false),
            new Category(savedUser, "Entertainment", "movie", false),
            new Category(savedUser, "Bills & Utilities", "receipt", false),
            new Category(savedUser, "Health & Fitness", "fitness_center", false),
            new Category(savedUser, "Investments", "trending_up", false),
            new Category(savedUser, "Salary / Income", "account_balance_wallet", false)
        );
        categoryRepository.saveAll(defaults);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(savedUser.getId(), savedUser.getUsername());

        return new AuthResponse(
                accessToken,
                refreshToken,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getDisplayName()
        );
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getCurrencySymbol(),
                user.getMonthlyBudget()
        );
    }
}
