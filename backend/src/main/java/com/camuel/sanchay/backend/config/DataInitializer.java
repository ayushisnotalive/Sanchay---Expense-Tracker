package com.camuel.sanchay.backend.config;

import com.camuel.sanchay.backend.entity.Category;
import com.camuel.sanchay.backend.entity.Role;
import com.camuel.sanchay.backend.entity.User;
import com.camuel.sanchay.backend.entity.Wallet;
import com.camuel.sanchay.backend.repository.CategoryRepository;
import com.camuel.sanchay.backend.repository.UserRepository;
import com.camuel.sanchay.backend.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           WalletRepository walletRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsersAndWallets();
        seedDefaultCategories();
    }

    private void seedUsersAndWallets() {
        if (userRepository.count() == 0) {
            log.info("Seeding initial enterprise demo users...");

            // Regular User
            User user = new User("user@sanchay.com", "user", passwordEncoder.encode("password123"), "Sanchay User");
            user.setCurrencySymbol("₹");
            user.setMonthlyBudget(50000.0);
            user.setRoles(Set.of(Role.ROLE_USER));
            User savedUser = userRepository.save(user);

            // Admin User
            User admin = new User("admin@sanchay.com", "admin", passwordEncoder.encode("admin123"), "Enterprise Admin");
            admin.setCurrencySymbol("₹");
            admin.setMonthlyBudget(100000.0);
            admin.setRoles(Set.of(Role.ROLE_USER, Role.ROLE_ADMIN));
            userRepository.save(admin);

            // Default Wallet for demo user
            Wallet defaultWallet = new Wallet();
            defaultWallet.setUser(savedUser);
            defaultWallet.setName("Primary Savings Account");
            defaultWallet.setBalance(25000.00);
            defaultWallet.setCurrency("₹");
            defaultWallet.setColorHex("#10B981");
            walletRepository.save(defaultWallet);

            log.info("Initial users and wallets seeded successfully.");
        }
    }

    private void seedDefaultCategories() {
        if (categoryRepository.count() == 0) {
            log.info("Seeding default institutional financial categories...");

            List<Category> defaultCategories = List.of(
                new Category(null, "Food & Dining", "fastfood", false),
                new Category(null, "Transportation", "directions_car", false),
                new Category(null, "Housing & Rent", "home", false),
                new Category(null, "Utilities", "bolt", false),
                new Category(null, "Entertainment", "movie", false),
                new Category(null, "Healthcare", "local_hospital", false),
                new Category(null, "Shopping", "shopping_bag", false),
                new Category(null, "Salary", "account_balance_wallet", false),
                new Category(null, "Investments", "trending_up", false)
            );

            categoryRepository.saveAll(defaultCategories);
            log.info("Default categories seeded successfully.");
        }
    }
}
