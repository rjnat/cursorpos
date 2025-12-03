package com.cursorpos.product.controller;

import com.cursorpos.product.dto.InventoryRequest;
import com.cursorpos.product.dto.StockAdjustmentRequest;
import com.cursorpos.product.entity.Category;
import com.cursorpos.product.entity.Inventory;
import com.cursorpos.product.entity.Product;
import com.cursorpos.product.repository.CategoryRepository;
import com.cursorpos.product.repository.InventoryRepository;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for InventoryController.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SuppressWarnings({ "null", "checkstyle:MethodName", "PMD.AvoidDuplicateLiterals" })
class InventoryControllerIntegrationTest {

    private static final String TEST_TENANT = "tenant-inventory-test-001";
    private static final long TOKEN_VALIDITY_MS = 60L * 60 * 1000;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String baseUrl;
    private HttpHeaders headers;
    private UUID testProductId;
    private UUID testBranchId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/inventory";
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
        testBranchId = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void testCreateOrUpdateInventory() {
        InventoryRequest request = InventoryRequest.builder()
                .productId(testProductId)
                .branchId(testBranchId)
                .quantityOnHand(100)
                .reorderPoint(20)
                .reorderQuantity(50)
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("\"success\":true");
        assertThat(response.getBody()).contains("Inventory created/updated successfully");
    }

    @Test
    void testAdjustStock() {
        // Create inventory first
        Product product = productRepository.findById(testProductId).orElseThrow();
        Inventory inventory = new Inventory();
        inventory.setTenantId(TEST_TENANT);
        inventory.setProduct(product);
        inventory.setBranchId(testBranchId);
        inventory.setQuantityOnHand(100);
        inventory.setReorderPoint(20);
        inventory.setReorderQuantity(50);
        inventory.setCreatedBy("test");
        inventoryRepository.save(inventory);

        StockAdjustmentRequest request = StockAdjustmentRequest.builder()
                .productId(testProductId)
                .branchId(testBranchId)
                .quantity(50)
                .type(StockAdjustmentRequest.AdjustmentType.ADD)
                .reason("Stock replenishment")
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/adjust",
                HttpMethod.POST,
                new HttpEntity<>(request, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
        assertThat(response.getBody()).contains("Stock adjusted successfully");
    }

    @Test
    void testReserveStock() {
        // Create inventory first
        Product product = productRepository.findById(testProductId).orElseThrow();
        Inventory inventory = new Inventory();
        inventory.setTenantId(TEST_TENANT);
        inventory.setProduct(product);
        inventory.setBranchId(testBranchId);
        inventory.setQuantityOnHand(100);
        inventory.setReorderPoint(20);
        inventory.setReorderQuantity(50);
        inventory.setCreatedBy("test");
        inventoryRepository.save(inventory);

        String url = String.format("%s/reserve?productId=%s&branchId=%s&quantity=10",
                baseUrl, testProductId, testBranchId);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
        assertThat(response.getBody()).contains("Stock reserved successfully");
    }

    @Test
    void testReleaseStock() {
        // Create inventory first with reserved stock
        Product product = productRepository.findById(testProductId).orElseThrow();
        Inventory inventory = new Inventory();
        inventory.setTenantId(TEST_TENANT);
        inventory.setProduct(product);
        inventory.setBranchId(testBranchId);
        inventory.setQuantityOnHand(100);
        inventory.setQuantityReserved(20);
        inventory.setReorderPoint(20);
        inventory.setReorderQuantity(50);
        inventory.setCreatedBy("test");
        inventoryRepository.save(inventory);

        String url = String.format("%s/release?productId=%s&branchId=%s&quantity=10",
                baseUrl, testProductId, testBranchId);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
        assertThat(response.getBody()).contains("Stock released successfully");
    }

    @Test
    void testGetInventoryById() {
        // Create inventory first
        Product product = productRepository.findById(testProductId).orElseThrow();
        Inventory inventory = new Inventory();
        inventory.setTenantId(TEST_TENANT);
        inventory.setProduct(product);
        inventory.setBranchId(testBranchId);
        inventory.setQuantityOnHand(100);
        inventory.setReorderPoint(20);
        inventory.setReorderQuantity(50);
        inventory.setCreatedBy("test");
        inventoryRepository.save(inventory);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + inventory.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    @Test
    void testGetInventoryByProductAndBranch() {
        // Create inventory first
        Product product = productRepository.findById(testProductId).orElseThrow();
        Inventory inventory = new Inventory();
        inventory.setTenantId(TEST_TENANT);
        inventory.setProduct(product);
        inventory.setBranchId(testBranchId);
        inventory.setQuantityOnHand(100);
        inventory.setReorderPoint(20);
        inventory.setReorderQuantity(50);
        inventory.setCreatedBy("test");
        inventoryRepository.save(inventory);

        String url = String.format("%s/product/%s/branch/%s",
                baseUrl, testProductId, testBranchId);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    @Test
    void testGetAllInventory() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    @Test
    void testGetInventoryByBranch() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/branch/" + testBranchId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    @Test
    void testGetInventoryByProduct() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/product/" + testProductId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    @Test
    void testGetLowStockItems() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/low-stock",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    @Test
    void testGetLowStockItemsByBranch() {
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/low-stock/branch/" + testBranchId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"success\":true");
    }

    private String createToken() {
        Date now = new Date();
        return Jwts.builder()
                .subject("test-user")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + TOKEN_VALIDITY_MS))
                .claim("tenant_id", TEST_TENANT)
                .claim("role", "ADMIN")
                .claim("permissions", List.of("INVENTORY_READ", "INVENTORY_WRITE"))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
