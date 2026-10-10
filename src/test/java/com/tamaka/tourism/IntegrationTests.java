package com.tamaka.tourism;

import com.tamaka.tourism.dto.*;
import com.tamaka.tourism.entity.*;
import com.tamaka.tourism.repository.*;
import com.tamaka.tourism.service.AuthService;
import com.tamaka.tourism.service.RecommendationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class IntegrationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("tourism")
            .withUsername("tourism")
            .withPassword("tourism");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("jwt.secret", () -> "test-secret-key-min-32-chars-long-for-testing");
        registry.add("jwt.expiration", () -> "86400000");
    }

    @Autowired
    AuthService authService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AttractionRepository attractionRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    RecommendationService recommendationService;

    @Autowired
    PasswordEncoder passwordEncoder;

    Category testCategory;
    User touristUser;

    @BeforeEach
    void setup() {
        attractionRepository.deleteAll();
        userRepository.deleteAll();
        
        testCategory = Category.builder()
                .name("Beach")
                .description("Beaches")
                .status(EntityStatus.ACTIVE)
                .build();
        testCategory = categoryRepository.save(testCategory);

        touristUser = User.builder()
                .email("tourist@test.com")
                .passwordHash(passwordEncoder.encode("Tourist123!"))
                .fullName("Test Tourist")
                .roles(Set.of(Role.TURISTA))
                .activeMode(ActiveMode.TURISTA)
                .status(UserStatus.ACTIVE)
                .isVerified(true)
                .build();
        touristUser = userRepository.save(touristUser);
    }

    @Test
    void registerAndLogin() {
        RegisterRequest registerRequest = new RegisterRequest("newuser@test.com", "Password123!", "New User");
        authService.register(registerRequest);

        var user = userRepository.findByEmail("newuser@test.com").orElseThrow();
        assertFalse(user.getIsVerified());

        Token token = user.getTokens().stream().filter(t -> t.getTokenType() == TokenType.VERIFICATION).findFirst().orElseThrow();
        authService.verify(new VerifyRequest(token.getToken()));

        user = userRepository.findByEmail("newuser@test.com").orElseThrow();
        assertTrue(user.getIsVerified());

        LoginResponse login = authService.login(new LoginRequest("newuser@test.com", "Password123!"));
        assertNotNull(login.token());
        assertEquals("newuser@test.com", login.email());
    }

    @Test
    void createAttractionAsGestor() {
        User gestor = User.builder()
                .email("gestor@test.com")
                .passwordHash(passwordEncoder.encode("Gestor123!"))
                .fullName("Test Gestor")
                .roles(Set.of(Role.GESTOR, Role.TURISTA))
                .activeMode(ActiveMode.TURISTA)
                .status(UserStatus.ACTIVE)
                .isVerified(true)
                .build();
        gestor = userRepository.save(gestor);

        AttractionRequest request = new AttractionRequest(
                "Test Beach", "Nice beach", "Santa Marta", 11.24, -74.20,
                List.of("img1.jpg"), "Easy access", "No pets", 100,
                Set.of(testCategory.getId()), null
        );

        AttractionResponse response = authService.createAttractionAsGestor(gestor, request);
        assertNotNull(response.id());
        assertEquals("Test Beach", response.name());
        assertEquals(OccupancyLevel.LOW.name(), response.occupancyLevel());
    }

    @Test
    void recommendationsWithHighOccupancyAlternatives() {
        // Create two attractions in same category
        Attraction a1 = Attraction.builder()
                .name("Crowded Beach")
                .description("Popular")
                .address("Santa Marta")
                .latitude(11.24)
                .longitude(-74.20)
                .status(EntityStatus.ACTIVE)
                .capacity(100)
                .currentOccupancy(90) // HIGH
                .categories(Set.of(testCategory))
                .build();

        Attraction a2 = Attraction.builder()
                .name("Empty Beach")
                .description("Quiet")
                .address("Santa Marta")
                .latitude(11.25)
                .longitude(-74.21)
                .status(EntityStatus.ACTIVE)
                .capacity(100)
                .currentOccupancy(10) // LOW
                .categories(Set.of(testCategory))
                .build();

        attractionRepository.save(a1);
        attractionRepository.save(a2);

        // Test alternatives for high occupancy
        var alternatives = recommendationService.getAlternativesForHighOccupancy(a1.getId());
        assertEquals(1, alternatives.items().size());
        assertEquals("Empty Beach", alternatives.items().get(0).item().name());
        assertTrue(alternatives.items().get(0).reason().contains("LOW occupancy"));
    }

    @Test
    void recommendationsScoring() {
        Attraction a1 = Attraction.builder()
                .name("Matching Low")
                .description("Matches category, low occupancy")
                .address("Santa Marta")
                .latitude(11.24)
                .longitude(-74.20)
                .status(EntityStatus.ACTIVE)
                .capacity(100)
                .currentOccupancy(20) // LOW
                .categories(Set.of(testCategory))
                .build();

        Attraction a2 = Attraction.builder()
                .name("NonMatching Medium")
                .description("Different category, medium occupancy")
                .address("Santa Marta")
                .latitude(11.25)
                .longitude(-74.21)
                .status(EntityStatus.ACTIVE)
                .capacity(100)
                .currentOccupancy(60) // MEDIUM
                .build();

        attractionRepository.save(a1);
        attractionRepository.save(a2);

        var result = recommendationService.recommendAttractions(touristUser, new RecommendationRequest(testCategory.getId(), null));
        assertEquals(2, result.items().size());
        assertEquals("Matching Low", result.items().get(0).item().name());
        assertTrue(result.items().get(0).score() > result.items().get(1).score());
    }
}