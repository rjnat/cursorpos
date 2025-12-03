package com.cursorpos.product.controller;

import com.cursorpos.product.dto.CategoryRequest;
import com.cursorpos.product.entity.Category;
import com.cursorpos.product.repository.CategoryRepository;
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

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings({ "null", "checkstyle:MethodName", "PMD.AvoidDuplicateLiterals" })
class CategoryControllerIntegrationTest {

    private static final String TEST_TENANT = "tenant-category-test-001";
    private static final long TOKEN_VALIDITY_MS = 60L * 60 * 1000;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CategoryRepository categoryRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1/categories";
        headers = new HttpHeaders();
        headers.setBearerAuth(createToken());
    }

    @AfterEach
    void tearDown() {
        categoryRepository.deleteAll();
    }

    @Test
    @Order(1)
    void testCreateCategory() {
        CategoryRequest request = CategoryRequest.builder()
                .code("HOT-DRINKS")
                .name("Hot Drinks")
                .description("All hot beverages")
                .isActive(true)
                .displayOrder(1)
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                authenticatedEntity(request),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(categoryRepository.count()).isEqualTo(1);
    }

    @Test
    @Order(2)
    void testCreateCategoryWithMissingCode() {
        CategoryRequest request = CategoryRequest.builder()
                .name("Invalid Category")
                .description("Missing code field")
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
    void testGetCategoryById() {
        Category category = createTestCategory("COLD-DRINKS", "Cold Drinks", null);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + category.getId(),
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("COLD-DRINKS");
    }

    @Test
    @Order(4)
    void testGetCategoryByCode() {
        createTestCategory("FOOD", "Food Items", null);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/code/FOOD",
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("FOOD");
    }

    @Test
    @Order(5)
    void testGetCategoryByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + nonExistentId,
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(6)
    void testGetAllCategories() {
        createTestCategory("CAT-1", "Category 1", null);
        createTestCategory("CAT-2", "Category 2", null);
        createTestCategory("CAT-3", "Category 3", null);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "?page=0&size=10",
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("CAT-1");
        assertThat(response.getBody()).contains("CAT-2");
        assertThat(response.getBody()).contains("CAT-3");
    }

    @Test
    @Order(7)
    void testGetSubcategories() {
        Category parent = createTestCategory("BEVERAGES", "Beverages", null);
        createTestCategory("TEA", "Tea", parent.getId());
        createTestCategory("COFFEE", "Coffee", parent.getId());

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + parent.getId() + "/subcategories",
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("TEA");
        assertThat(response.getBody()).contains("COFFEE");
    }

    @Test
    @Order(8)
    void testGetActiveCategories() {
        Category active = createTestCategory("ACTIVE-1", "Active Category", null);
        active.setIsActive(true);
        categoryRepository.save(active);

        Category inactive = createTestCategory("INACTIVE-1", "Inactive Category", null);
        inactive.setIsActive(false);
        categoryRepository.save(inactive);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/active",
                HttpMethod.GET,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("ACTIVE-1");
        assertThat(response.getBody()).doesNotContain("INACTIVE-1");
    }

    @Test
    @Order(9)
    void testUpdateCategory() {
        Category category = createTestCategory("UPDATE-TEST", "Original Name", null);

        CategoryRequest updateRequest = CategoryRequest.builder()
                .code("UPDATE-TEST")
                .name("Updated Name")
                .description("Updated description")
                .isActive(true)
                .displayOrder(5)
                .build();

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + category.getId(),
                HttpMethod.PUT,
                authenticatedEntity(updateRequest),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Category updated = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    @Order(10)
    void testDeleteCategory() {
        Category category = createTestCategory("DELETE-TEST", "To Be Deleted", null);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + category.getId(),
                HttpMethod.DELETE,
                authenticatedEntity(),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Category deleted = categoryRepository.findById(category.getId()).orElseThrow();
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
                .claim("permissions", List.of("CATEGORY_READ", "CATEGORY_WRITE"))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private Category createTestCategory(String code, String name, UUID parentId) {
        Category category = new Category();
        category.setTenantId(TEST_TENANT);
        category.setCode(code);
        category.setName(name);
        category.setDescription("Test description for " + name);
        if (parentId != null) {
            Category parent = categoryRepository.findById(parentId).orElse(null);
            category.setParent(parent);
        }
        category.setIsActive(true);
        category.setDisplayOrder(1);
        category.setCreatedBy(TEST_TENANT);
        return categoryRepository.save(category);
    }
}
