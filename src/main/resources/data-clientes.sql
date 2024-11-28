
-- CLIENTE Nº1
    -- agencia: 2345-6
    -- conta: 7654321-0
    -- senha: senha123
    
INSERT INTO cliente_dados (
    id_cliente, nome, cpf, email, senha, telefone, data_de_nascimento,
    cep, estado, cidade, bairro, rua,numero_residencia, role, pcd, sexo, escolaridade,
    estado_civil, ocupacao, renda_mensal
) VALUES (
    1, 'Pedro Paulo', '551.571.464-42', 'ppdev@gmail.com', '$2a$10$5j3dZ5D7MblH0GXF7aqv5ulZkAdjMAdSGvceppMWxi.q.A3UX7Twe',
    '(81)99342-6942', '1997-05-10', '50800-010', 'PE', 'Recife',
    'Iputinga', 'Rua Professor Joaquim Cavalcanti', '48', 'ROLE_USER', null, 'MASCULINO',
    'ENSINO_SUPERIOR', 'SOLTEIRO', 'DESENVOLVEDOR_DE_SOFTWARE', 15720.95
);

INSERT INTO cliente_perfil (id_perfil, nota_do_perfil, score, avaliacao, id_cliente)
VALUES (1, 7.4, 766, 'BOM', 1);

INSERT INTO cliente_conta (id_conta, agencia, numero_da_conta, saldo, id_cliente)
VALUES (1, "2345-6", "7654321-0", 15720.95, 1);





-- CLIENTE Nº2
    -- agencia: 1334-9
    -- conta: 1234567-8
    -- senha odontoPorAmorS2

INSERT INTO cliente_dados (
    id_cliente, nome, cpf, email, senha, telefone, data_de_nascimento,
    cep, estado, cidade, bairro, rua, numero_residencia, role, pcd, sexo, escolaridade,
    estado_civil, ocupacao, renda_mensal
) VALUES (
    2, 'Maria Silva', '123.456.789-00', 'maria.silva@gmail.com', '$2a$10$t05kLC0IdBV9LVQMD.6YiuYhThpZ7shjtJjhfm4D/FtWvqr/xFxW2',
    '(81)91234-5678', '1990-08-15', '52390-210', 'PE', 'Recife',
    'Passarinho', 'Rua Santa Fé', '100', 'ROLE_USER', null, 'FEMININO',
    'POS_GRADUACAO', 'CASADO', 'DENTISTA', 6500.00
);

INSERT INTO cliente_perfil (id_perfil, nota_do_perfil, score, avaliacao, id_cliente)
VALUES (2, 3.2, 315, 'RUIM', 2);

INSERT INTO cliente_conta (id_conta, agencia, numero_da_conta, saldo, id_cliente)
VALUES (2, "1334-9", "1234567-8", 6500.00, 2);
