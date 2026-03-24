package com.foodcost.ingredient

import com.foodcost.ingredient.dto.IngredientCreateRequest
import com.foodcost.ingredient.dto.IngredientUpdateRequest
import com.foodcost.ingredient.entity.Ingredient
import com.foodcost.ingredient.repository.IngredientRepository
import com.foodcost.ingredient.service.DuplicateIngredientException
import com.foodcost.ingredient.service.IngredientNotFoundException
import com.foodcost.ingredient.service.IngredientService
import com.foodcost.ingredient.service.InvalidUnitException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IngredientServiceTest {

    private val ingredientRepository = mockk<IngredientRepository>()
    private val ingredientService = IngredientService(ingredientRepository)

    private val tenantId = UUID.randomUUID()

    @Test
    fun `create_withValidRequest_savesAndReturnsDto`() {
        val request = IngredientCreateRequest(name = "  Farina 00  ", unit = "kg", price = BigDecimal("1.2500"))
        val now = Instant.now()
        val savedId = UUID.randomUUID()

        val ingredientSlot = slot<Ingredient>()
        every { ingredientRepository.existsByTenantIdAndNameIgnoreCase(tenantId, "Farina 00") } returns false
        every { ingredientRepository.save(capture(ingredientSlot)) } answers {
            Ingredient(
                id = savedId,
                tenantId = ingredientSlot.captured.tenantId,
                name = ingredientSlot.captured.name,
                unit = ingredientSlot.captured.unit,
                price = ingredientSlot.captured.price,
                createdAt = now,
                updatedAt = now,
            )
        }

        val result = ingredientService.create(request, tenantId)

        assertNotNull(result)
        assertEquals(savedId, result.id)
        assertEquals("Farina 00", result.name) // trimmed
        assertEquals("kg", result.unit)
        assertEquals(BigDecimal("1.2500"), result.price)
        assertEquals(tenantId, ingredientSlot.captured.tenantId)

        verify(exactly = 1) { ingredientRepository.existsByTenantIdAndNameIgnoreCase(tenantId, "Farina 00") }
        verify(exactly = 1) { ingredientRepository.save(any()) }
    }

    @Test
    fun `create_withDuplicateName_throwsDuplicateIngredientException`() {
        val request = IngredientCreateRequest(name = "Pomodoro", unit = "kg", price = BigDecimal("2.50"))

        every { ingredientRepository.existsByTenantIdAndNameIgnoreCase(tenantId, request.name) } returns true

        assertThrows<DuplicateIngredientException> {
            ingredientService.create(request, tenantId)
        }

        verify(exactly = 0) { ingredientRepository.save(any()) }
    }

    @Test
    fun `create_withInvalidUnit_throwsInvalidUnitException`() {
        val request = IngredientCreateRequest(name = "Sale", unit = "invalid_unit", price = BigDecimal("0.50"))

        assertThrows<InvalidUnitException> {
            ingredientService.create(request, tenantId)
        }

        verify(exactly = 0) { ingredientRepository.existsByTenantIdAndNameIgnoreCase(any(), any()) }
        verify(exactly = 0) { ingredientRepository.save(any()) }
    }

    @Test
    fun `findAll_returnsSortedIngredients`() {
        val now = Instant.now()
        val ingredients = listOf(
            Ingredient(id = UUID.randomUUID(), tenantId = tenantId, name = "Basilico", unit = "g", price = BigDecimal("0.0350"), createdAt = now, updatedAt = now),
            Ingredient(id = UUID.randomUUID(), tenantId = tenantId, name = "Farina", unit = "kg", price = BigDecimal("1.20"), createdAt = now.minusSeconds(60), updatedAt = now.minusSeconds(60)),
        )

        every { ingredientRepository.findByTenantIdOrderByCreatedAtDesc(tenantId) } returns ingredients

        val result = ingredientService.findAll(tenantId)

        assertEquals(2, result.size)
        assertEquals("Basilico", result[0].name)
        assertEquals("Farina", result[1].name)

        verify(exactly = 1) { ingredientRepository.findByTenantIdOrderByCreatedAtDesc(tenantId) }
    }

    // --- update tests ---

    private fun existingIngredient(
        id: UUID = UUID.randomUUID(),
        name: String = "Farina 00",
        unit: String = "kg",
        price: BigDecimal = BigDecimal("1.2500"),
        tenant: UUID = tenantId,
    ): Ingredient {
        val now = Instant.now()
        return Ingredient(
            id = id,
            tenantId = tenant,
            name = name,
            unit = unit,
            price = price,
            createdAt = now,
            updatedAt = now,
        )
    }

    @Test
    fun `update_withValidRequest_updatesAndReturnsDto`() {
        val ingredientId = UUID.randomUUID()
        val ingredient = existingIngredient(id = ingredientId)
        val request = IngredientUpdateRequest(name = "Farina 0", unit = "g", price = BigDecimal("2.00"))

        every { ingredientRepository.findByIdAndTenantId(ingredientId, tenantId) } returns ingredient
        every { ingredientRepository.existsByTenantIdAndNameIgnoreCase(tenantId, "Farina 0") } returns false
        every { ingredientRepository.save(any()) } answers { firstArg() }

        val result = ingredientService.update(ingredientId, request, tenantId)

        assertEquals("Farina 0", result.name)
        assertEquals("g", result.unit)
        assertEquals(BigDecimal("2.00"), result.price)
        assertEquals(ingredientId, result.id)

        verify(exactly = 1) { ingredientRepository.save(any()) }
    }

    @Test
    fun `update_withSameNameDifferentCase_doesNotThrowDuplicate`() {
        val ingredientId = UUID.randomUUID()
        val ingredient = existingIngredient(id = ingredientId, name = "Farina")
        val request = IngredientUpdateRequest(name = "farina", unit = "kg", price = BigDecimal("1.50"))

        every { ingredientRepository.findByIdAndTenantId(ingredientId, tenantId) } returns ingredient
        every { ingredientRepository.save(any()) } answers { firstArg() }

        val result = ingredientService.update(ingredientId, request, tenantId)

        assertEquals("farina", result.name)
        verify(exactly = 0) { ingredientRepository.existsByTenantIdAndNameIgnoreCase(any(), any()) }
    }

    @Test
    fun `update_withDuplicateNameDifferentIngredient_throwsDuplicateIngredientException`() {
        val ingredientId = UUID.randomUUID()
        val ingredient = existingIngredient(id = ingredientId, name = "Farina")
        val request = IngredientUpdateRequest(name = "Pomodoro", unit = "kg", price = BigDecimal("2.50"))

        every { ingredientRepository.findByIdAndTenantId(ingredientId, tenantId) } returns ingredient
        every { ingredientRepository.existsByTenantIdAndNameIgnoreCase(tenantId, "Pomodoro") } returns true

        assertThrows<DuplicateIngredientException> {
            ingredientService.update(ingredientId, request, tenantId)
        }

        verify(exactly = 0) { ingredientRepository.save(any()) }
    }

    @Test
    fun `update_withInvalidUnit_throwsInvalidUnitException`() {
        val ingredientId = UUID.randomUUID()
        val request = IngredientUpdateRequest(name = "Sale", unit = "invalid_unit", price = BigDecimal("0.50"))

        assertThrows<InvalidUnitException> {
            ingredientService.update(ingredientId, request, tenantId)
        }

        verify(exactly = 0) { ingredientRepository.findByIdAndTenantId(any(), any()) }
    }

    @Test
    fun `update_withNonExistentId_throwsIngredientNotFoundException`() {
        val ingredientId = UUID.randomUUID()
        val request = IngredientUpdateRequest(name = "Sale", unit = "kg", price = BigDecimal("0.50"))

        every { ingredientRepository.findByIdAndTenantId(ingredientId, tenantId) } returns null

        assertThrows<IngredientNotFoundException> {
            ingredientService.update(ingredientId, request, tenantId)
        }

        verify(exactly = 0) { ingredientRepository.save(any()) }
    }

    @Test
    fun `update_withWrongTenantId_throwsIngredientNotFoundException`() {
        val ingredientId = UUID.randomUUID()
        val wrongTenantId = UUID.randomUUID()
        val request = IngredientUpdateRequest(name = "Sale", unit = "kg", price = BigDecimal("0.50"))

        every { ingredientRepository.findByIdAndTenantId(ingredientId, wrongTenantId) } returns null

        assertThrows<IngredientNotFoundException> {
            ingredientService.update(ingredientId, request, wrongTenantId)
        }

        verify(exactly = 0) { ingredientRepository.save(any()) }
    }
}
