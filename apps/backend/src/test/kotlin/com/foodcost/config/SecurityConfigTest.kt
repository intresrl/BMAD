package com.foodcost.config

import com.foodcost.auth.service.AuthService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var authService: AuthService

    @Test
    fun `protected endpoint without JWT returns 401`() {
        mockMvc.get("/api/v1/protected-resource").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `actuator health is publicly accessible`() {
        mockMvc.get("/actuator/health").andExpect {
            status { isOk() }
        }
    }
}
