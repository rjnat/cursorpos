package com.cursorpos.product.controller;

import com.cursorpos.product.dto.ProductRequest;
import com.cursorpos.product.entity.Category;
import com.cursorpos.product.entity.Product;
import com.cursorpos.product.repository.CategoryRepository;
import com.cursorpos.product.repository.ProductRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings({ "null", "checkstyle:MethodName", "PMD.AvoidDuplicateLiterals" })
class ProductControllerIntegrationTest {

    private static final String TEST_TENANT = "tenant-product-test-001";
    private static final long TOKEN_VALIDITY_MS = 60L * 60 * 1000;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String baseUrl;
    private HttpHeaders headers;
    private UUID testCategoryId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/products";
        headers = new HttpHeaders();
        headers.setBearerAuth(createToken());

        Category category = new Category();
        category.setTenantId(TEST_TENANT);
        category.setCode("TEST-CAT");
        category.setName("Test Category");
        category.setIsActive(true);
        category.setCreatedBy(TEST_TENANT);
        testCategoryId = categoryRepository.save(category).getId();
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @Order(1)
    void testCreateProduct() {
        ProductRequest request = buildProductRequest("PROD-001", "SKU-001", "Test Product");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                authenticatedEntity(request),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("PROD-001");
        assertThat(productRepository.count()).isEqualTo(1);
    }

    @Test
    @Order(2)
    void testCreateProductWithMissingFields() {
        ProductRequest request = ProductRequest.builder()
                .name("Invalid Product")
                .price(new BigDecimal("10.00"))
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                authenticatedEntity(request),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(3)
    void testCreateProductWithInvalidPrice() {
        ProductRequest request = ProductRequest.builder()
                .code("PROD-002")
                .sku("SKU-002")
                .name("Invalid Price Product")
                .price(new BigDecimal("-10.00"))
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                authenticatedEntity(request),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(4)
    void testGetProductById() {
        Product product = createTestProduct("PROD-003", "SKU-003", "Product 3");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + product.getId(),
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("PROD-003");
    }

    @Test
    @Order(5)
    void testGetProductByCode() {
        createTestProduct("PROD-004", "SKU-004", "Product 4");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/code/PROD-004",
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("PROD-004");
    }

    @Test
    @Order(6)
    void testGetProductBySku() {
        createTestProduct("PROD-005", "SKU-005", "Product 5");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/sku/SKU-005",
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("SKU-005");
    }

    @Test
    @Order(7)
    void testGetProductByBarcode() {
        createTestProduct("PROD-006", "SKU-006", "Product 6");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/barcode/1234567890123",
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("1234567890123");
    }

    @Test
    @Order(8)
    void testGetProductByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + nonExistentId,
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(9)
    void testGetAllProducts() {
        createTestProduct("PROD-007", "SKU-007", "Product 7");
        createTestProduct("PROD-008", "SKU-008", "Product 8");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "?page=0&size=10",
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("PROD-007");
        assertThat(response.getBody()).contains("PROD-008");
    }

    @Test
    @Order(10)
    void testGetProductsByCategory() {
        createTestProduct("PROD-009", "SKU-009", "Product 9");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/category/" + testCategoryId + "?page=0&size=10",
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("PROD-009");
    }

    @Test
    @Order(11)
    void testGetActiveProducts() {
        Product active = createTestProduct("PROD-010", "SKU-010", "Active");
        active.setIsActive(true);
        productRepository.save(active);

        Product inactive = createTestProduct("PROD-011", "SKU-011", "Inactive");
        inactive.setIsActive(false);
        productRepository.save(inactive);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/active",
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("PROD-010");
        assertThat(response.getBody()).doesNotContain("PROD-011");
    }

    @Test
    @Order(12)
    void testSearchProducts() {
        createTestProduct("PROD-012", "SKU-012", "Searchable Product");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/search?query=Searchable&page=0&size=10",
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Searchable Product");
    }

    @Test
    @Order(13)
    void testUpdateProduct() {
        Product product = createTestProduct("PROD-013", "SKU-013", "Original");

        ProductRequest updateRequest = ProductRequest.builder()
                .code("PROD-013")
                .sku("SKU-013")
                .name("Updated Product")
                .description("Updated description")
                .categoryId(testCategoryId)
                .price(new BigDecimal("30.00"))
                .cost(new BigDecimal("18.00"))
                .taxRate(new BigDecimal("12.5"))
                .unit("piece")
                .isActive(true)
                .isTrackable(true)
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + product.getId(),
                HttpMethod.PUT,
                authenticatedEntity(updateRequest),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Updated Product");
    }

    @Test
    @Order(14)
    void testDeleteProduct() {
        Product product = createTestProduct("PROD-014", "SKU-014", "DeleteMe");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + product.getId(),
                HttpMethod.DELETE,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Product deleted = productRepository.findById(product.getId()).orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }

    private <T> HttpEntity<T> authenticatedEntity(T body) {
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Void> authenticatedEntity() {
        return new HttpEntity<>(headers);
    }

    private String createToken() {
        Date now = new Date();
        return Jwts.builder()
                .subject("integration-test-user")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + TOKEN_VALIDITY_MS))
                .claim("tenant_id", TEST_TENANT)
                .claim("role", "ADMIN")
                .claim("permissions", List.of("PRODUCT_READ", "PRODUCT_WRITE"))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private ProductRequest buildProductRequest(String code, String sku, String name) {
        return ProductRequest.builder()
                .code(code)
                .sku(sku)
                .name(name)
                .description("Test product description")
                .categoryId(testCategoryId)
                .price(new BigDecimal("25.99"))
                .cost(new BigDecimal("15.00"))
                .taxRate(new BigDecimal("10.0"))
                .unit("piece")
                .barcode("1234567890123")
                .isActive(true)
                .isTrackable(true)
                .minStockLevel(10)
                .maxStockLevel(100)
                .build();
    }

    private Product createTestProduct(String code, String sku, String name) {
        Product product = new Product();
        product.setTenantId(TEST_TENANT);
        product.setCode(code);
        product.setSku(sku);
        product.setName(name);
        product.setPrice(new BigDecimal("19.99"));
        product.setCost(new BigDecimal("9.00"));
        product.setBarcode("1234567890123");
        product.setIsActive(true);
        product.setIsTrackable(true);
        Category category = categoryRepository.findById(testCategoryId).orElse(null);
        product.setCategory(category);
        product.setCreatedBy(TEST_TENANT);
        return productRepository.save(product);
    }
}
