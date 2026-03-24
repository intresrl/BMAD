CREATE TABLE ingredients (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID         NOT NULL REFERENCES tenants(id),
    name        VARCHAR(255) NOT NULL,
    unit        VARCHAR(50)  NOT NULL,
    price       NUMERIC(12,4) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uq_ingredients_tenant_name UNIQUE (tenant_id, name)
);

CREATE INDEX idx_ingredients_tenant_id ON ingredients(tenant_id);
