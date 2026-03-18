# FoodCost SaaS — Repo Notes

## Stack & Versions
- Spring Boot 4.0.3 + Kotlin 2.2.21 + Gradle 9.3.1
- Angular 21 + Angular Material 21 + Tailwind v4 + Vitest
- Jackson 3.x (group: `tools.jackson`), not `com.fasterxml.jackson`

## Kotlin K2 Compiler Gotchas
- Java platform types (`String!`) from Spring Security are inferred as `String?` with `-Xjsr305=strict`. Use `!!` for known non-null returns (e.g. `passwordEncoder.encode()!!`)
- `@field:` annotations required for data class validation (e.g. `@field:Email`, `@field:Size`)

## Spring Boot 4.x Package Changes
- `AutoConfigureMockMvc` → `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`
- `WebMvcTest` → `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
- `@MockitoBean` → `org.springframework.test.context.bean.override.mockito.MockitoBean`
- `ObjectMapper` auto-bean not available in test context — use inline JSON strings or manual creation

## Test Configuration
- Use `@SpringBootTest` + `@AutoConfigureMockMvc` (not `@WebMvcTest` — had issues in Boot 4.x)
- H2 in test: `spring.flyway.enabled=false`, `spring.jpa.hibernate.ddl-auto=none`
- `mockito-kotlin:5.4.0` added for Kotlin-friendly any() and whenever()
- MockK 1.14.0 for unit tests

## Angular 21 Patterns
- No `standalone: true/false` in @Component — implicit standalone
- Use `inject()` at field level instead of constructor DI for fields used in initializers
- `ChangeDetectionStrategy.OnPush` mandatory on all components
- `fc-` selector prefix
