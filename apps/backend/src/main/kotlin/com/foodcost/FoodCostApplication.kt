package com.foodcost

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FoodCostApplication

fun main(args: Array<String>) {
	runApplication<FoodCostApplication>(*args)
}
