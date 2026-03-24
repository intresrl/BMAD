package com.foodcost.ingredient.service

class InvalidUnitException(unit: String) : RuntimeException("Invalid unit: $unit")
