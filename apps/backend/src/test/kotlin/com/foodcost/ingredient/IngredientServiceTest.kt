package com.foodcost.ingredient

import com.foodcost.ingredient.dto.IngredientCreateRequest
import com.foodcost.ingredient.entity.Ingredient
import com.foodcost.ingredient.repository.IngredientRepository
import com.foodcost.ingredient.service.DuplicateIngredientException
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
}
