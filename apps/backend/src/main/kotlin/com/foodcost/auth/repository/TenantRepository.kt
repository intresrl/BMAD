package com.foodcost.auth.repository

import com.foodcost.auth.entity.Tenant
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TenantRepository : JpaRepository<Tenant, UUID>
