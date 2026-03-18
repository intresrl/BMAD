package com.foodcost

import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class FoodCostApplicationTests {

	@Test
	fun applicationClassHasSpringBootAnnotation() {
		val annotation = FoodCostApplication::class.java.getAnnotation(
			org.springframework.boot.autoconfigure.SpringBootApplication::class.java
		)
		assertNotNull(annotation, "FoodCostApplication must be annotated with @SpringBootApplication")
	}
}
