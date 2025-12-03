package com.cursorpos.product.controller;

import com.cursorpos.product.entity.Category;
import com.cursorpos.product.entity.PriceHistory;
import com.cursorpos.product.entity.Product;
import com.cursorpos.product.repository.CategoryRepository;
import com.cursorpos.product.repository.PriceHistoryRepository;
import com.cursorpos.product.repository.ProductRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for PriceHistoryController.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SuppressWarnings({ "null", "checkstyle:MethodName", "PMD.AvoidDuplicateLiterals" })
class PriceHistoryControllerIntegrationTest {

    private static final String TEST_TENANT = "tenant-pricehistory-test-001";
    private static final long TOKEN_VALIDITY_MS = 60L * 60 * 1000;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PriceHistoryRepository priceHistoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String baseUrl;
    private HttpHeaders headers;
    private UUID testProductId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/price-history";
        headers = new HttpHeaders();
        headers.setBearerAuth(createToken());

        // Create test category and product
        Category category = new Category();
        category.setTenantId(TEST_TENANT);
        category.setCode("BEVERAGES");
        category.setName("Beverages");
        category.setIsActive(true);
        category.setCreatedBy("test");
        categoryRepository.save(category);

        Product product = new Product();
        product.setTenantId(TEST_TENANT);
        product.setCode("PROD-001");
        product.setSku("TEST-SKU-001");
        product.setName("Test Product");
        product.setCategory(category);
        product.setPrice(BigDecimal.valueOf(100));
        product.setIsActive(true);
        product.setCreatedBy("test");
        productRepository.save(product);

        testProductId = product.getId();
    }

    @AfterEach
    void tearDown() {
        priceHistoryRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void testGetPriceHistory() {
        // Create price history entries
        Product product = productRepository.findById(testProductId).orElseThrow();
        PriceHistory history1 = new PriceHistory();
        history1.setTenantId(TEST_TENANT);
        history1.setProduct(product);
        history1.setNewPrice(BigDecimal.valueOf(100));
        history1.setEffectiveFrom(LocalDateTime.now().minusDays(10));
        history1.setCreatedBy("test");
        priceHistoryRepository.save(history1);

        PriceHistory history2 = new PriceHistory();
        history2.setTenantId(TEST_TENANT);
        history2.setProduct(product);
        history2.setNewPrice(BigDecimal.valueOf(120));
        history2.setEffectiveFrom(LocalDateTime.now().minusDays(5));
        history2.setCreatedBy("test");
        priceHistoryRepository.save(history2);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/product/" + testProductId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    @Test
    void testGetEffectivePrice_Found() {
        // Create price history entry
        Product product = productRepository.findById(testProductId).orElseThrow();
        PriceHistory history = new PriceHistory();
        history.setTenantId(TEST_TENANT);
        history.setProduct(product);
        history.setNewPrice(BigDecimal.valueOf(100));
        history.setEffectiveFrom(LocalDateTime.now().minusDays(5));
        history.setCreatedBy("test");
        priceHistoryRepository.save(history);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/product/" + testProductId + "/effective",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    @Test
    void testGetEffectivePrice_NotFound() {
        // No price history exists
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/product/" + testProductId + "/effective",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private String createToken() {
        Date now = new Date();
        return Jwts.builder()
                .subject("test-user")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + TOKEN_VALIDITY_MS))
                .claim("tenant_id", TEST_TENANT)
                .claim("role", "ADMIN")
                .claim("permissions", List.of("PRICE_READ"))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
