DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM tb_contas
        WHERE saldo IS NOT NULL AND saldo <> ROUND(saldo, 2)
    ) OR EXISTS (
        SELECT 1 FROM tb_contas_correntes
        WHERE limite IS NOT NULL AND limite <> ROUND(limite, 2)
    ) OR EXISTS (
        SELECT 1 FROM tb_transacoes
        WHERE valor IS NOT NULL AND valor <> ROUND(valor, 2)
    ) THEN
        RAISE EXCEPTION 'Monetary values with more than two fractional digits must be corrected before migration';
    END IF;
END
$$;

ALTER TABLE tb_contas
    ALTER COLUMN saldo TYPE NUMERIC(19, 2) USING saldo::NUMERIC(19, 2);

ALTER TABLE tb_contas_correntes
    ALTER COLUMN limite TYPE NUMERIC(19, 2) USING limite::NUMERIC(19, 2);

ALTER TABLE tb_transacoes
    ALTER COLUMN valor TYPE NUMERIC(19, 2) USING valor::NUMERIC(19, 2);

ALTER TABLE tb_contas
    ADD COLUMN IF NOT EXISTS status VARCHAR(255) DEFAULT 'ATIVA';

UPDATE tb_contas SET status = 'ATIVA' WHERE status IS NULL;

ALTER TABLE tb_contas
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE tb_transacoes
    ADD COLUMN IF NOT EXISTS conta_contraparte_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_tb_transacoes_conta_contraparte'
          AND conrelid = 'tb_transacoes'::regclass
    ) THEN
        ALTER TABLE tb_transacoes
            ADD CONSTRAINT fk_tb_transacoes_conta_contraparte
            FOREIGN KEY (conta_contraparte_id) REFERENCES tb_contas (id);
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_tb_transacoes_conta_contraparte_id
    ON tb_transacoes (conta_contraparte_id);
