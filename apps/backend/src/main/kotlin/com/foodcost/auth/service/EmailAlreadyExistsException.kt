package com.foodcost.auth.service

class EmailAlreadyExistsException :
    RuntimeException("An account with this email already exists")
