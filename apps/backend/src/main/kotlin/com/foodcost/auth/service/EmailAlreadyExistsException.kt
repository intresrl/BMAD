package com.foodcost.auth.service

class EmailAlreadyExistsException(email: String) :
    RuntimeException("An account with this email already exists: $email")
