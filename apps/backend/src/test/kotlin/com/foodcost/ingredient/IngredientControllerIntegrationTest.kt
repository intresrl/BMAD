package com.foodcost.ingredient

import com.foodcost.ingredient.dto.IngredientDto
import com.foodcost.ingredient.service.DuplicateIngredientException
import com.foodcost.ingredient.service.IngredientNotFoundException
import com.foodcost.ingredient.service.IngredientService
import com.foodcost.ingredient.service.InvalidUnitException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.math.BigDecimal
import java.time.Instant
import java.util.Date
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
class IngredientControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var ingredientService: IngredientService

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.issuer}")
    private lateinit var jwtIssuer: String

    private val tenantId = UUID.randomUUID()
    private val userId = UUID.randomUUID()

    private fun generateTestJwt(
        sub: UUID = userId,
        tenant: UUID = tenantId,
        roles: String = "ROLE_USER",
    ): String {
        val key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))
        return Jwts.builder()
            .subject(sub.toString())
            .issuer(jwtIssuer)
            .claim("tenantId", tenant.toString())
            .claim("roles", roles)
            .claim("plan", "base")
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + 15 * 60 * 1000))
            .signWith(key)
            .compact()
    }

    @Test
    fun `POST ingredients with valid body and JWT returns 201 and IngredientDto`() {
        val now = Instant.now()
        val ingredientId = UUID.randomUUID()
        val dto = IngredientDto(
            id = ingredientId,
            name = "Farina 00",
            unit = "kg",
            price = BigDecimal("1.2500"),
            createdAt = now,
            updatedAt = now,
        )

        whenever(ingredientService.create(any(), eq(tenantId))).thenReturn(dto)

        mockMvc.post("/api/v1/ingredients") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer ${generateTestJwt()}")
            content = """{"name":"Farina 00","unit":"kg","price":1.2500}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { value(ingredientId.toString()) }
            jsonPath("$.name") { value("Farina 00") }
            jsonPath("$.unit") { value("kg") }
            jsonPath("$.price") { value(1.25) }
            jsonPath("$.tenantId") { doesNotExist() }
        }
    }

    @Test
    fun `POST ingredients with duplicate name returns 422 RFC 7807`() {
        whenever(ingredientService.create(any(), eq(tenantId)))
            .thenThrow(DuplicateIngredientException())

        mockMvc.post("/api/v1/ingredients") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer ${generateTestJwt()}")
            content = """{"name":"Pomodoro","unit":"kg","price":2.50}"""
        }.andExpect {
            status { isUnprocessableEntity() }
            jsonPath("$.status") { value(422) }
            jsonPath("$.title") { value("Unprocessable Entity") }
            jsonPath("$.detail") { value("An ingredient with this name already exists in your warehouse") }
        }
    }

    @Test
    fun `POST ingredients with negative price returns 400`() {
        mockMvc.post("/api/v1/ingredients") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer ${generateTestJwt()}")
            content = """{"name":"Sale","unit":"kg","price":-1.00}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
        }
    }

    @Test
    fun `POST ingredients without JWT returns 401`() {
        mockMvc.post("/api/v1/ingredients") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Sale","unit":"kg","price":1.00}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `GET ingredients with valid JWT returns 200 and list`() {
        val now = Instant.now()
        val ingredients = listOf(
            IngredientDto(UUID.randomUUID(), "Basilico", "g", BigDecimal("0.0350"), now, now),
            IngredientDto(UUID.randomUUID(), "Farina", "kg", BigDecimal("1.20"), now, now),
        )

        whenever(ingredientService.findAll(eq(tenantId))).thenReturn(ingredients)

        mockMvc.get("/api/v1/ingredients") {
            header("Authorization", "Bearer ${generateTestJwt()}")
        }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].name") { value("Basilico") }
            jsonPath("$[1].name") { value("Farina") }
            jsonPath("$[0].tenantId") { doesNotExist() }
        }
    }

    // --- PUT /api/v1/ingredients/{id} ---

    @Test
    fun `PUT ingredients_id with valid body and JWT returns 200 and updated IngredientDto`() {
        val ingredientId = UUID.randomUUID()
        val now = Instant.now()
        val dto = IngredientDto(
            id = ingredientId,
            name = "Farina 0",
            unit = "g",
            price = BigDecimal("2.0000"),
            createdAt = now,
            updatedAt = now,
        )

        whenever(ingredientService.update(eq(ingredientId), any(), eq(tenantId))).thenReturn(dto)

        mockMvc.put("/api/v1/ingredients/$ingredientId") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer ${generateTestJwt()}")
            content = """{"name":"Farina 0","unit":"g","price":2.0000}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(ingredientId.toString()) }
            jsonPath("$.name") { value("Farina 0") }
            jsonPath("$.unit") { value("g") }
            jsonPath("$.price") { value(2.0) }
        }
    }

    @Test
    fun `PUT ingredients_id with duplicate name returns 422 RFC 7807`() {
        val ingredientId = UUID.randomUUID()

        whenever(ingredientService.update(eq(ingredientId), any(), eq(tenantId)))
            .thenThrow(DuplicateIngredientException())

        mockMvc.put("/api/v1/ingredients/$ingredientId") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer ${generateTestJwt()}")
            content = """{"name":"Pomodoro","unit":"kg","price":2.50}"""
        }.andExpect {
            status { isUnprocessableEntity() }
            jsonPath("$.status") { value(422) }
            jsonPath("$.detail") { value("An ingredient with this name already exists in your warehouse") }
        }
    }

    @Test
    fun `PUT ingredients_id with negative price returns 400`() {
        val ingredientId = UUID.randomUUID()

        mockMvc.put("/api/v1/ingredients/$ingredientId") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer ${generateTestJwt()}")
            content = """{"name":"Sale","unit":"kg","price":-1.00}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.status") { value(400) }
        }
    }

    @Test
    fun `PUT ingredients_id with non-existent id returns 404`() {
        val ingredientId = UUID.randomUUID()

        whenever(ingredientService.update(eq(ingredientId), any(), eq(tenantId)))
            .thenThrow(IngredientNotFoundException())

        mockMvc.put("/api/v1/ingredients/$ingredientId") {
            contentType = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer ${generateTestJwt()}")
            content = """{"name":"Sale","unit":"kg","price":1.00}"""
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.status") { value(404) }
            jsonPath("$.detail") { value("Ingredient not found") }
        }
    }

    @Test
    fun `PUT ingredients_id without JWT returns 401`() {
        val ingredientId = UUID.randomUUID()

        mockMvc.put("/api/v1/ingredients/$ingredientId") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Sale","unit":"kg","price":1.00}"""
        }.andExpect {
            status { isUnauthorized() }
        }
    }
}
