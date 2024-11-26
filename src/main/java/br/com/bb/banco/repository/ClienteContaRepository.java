package br.com.bb.banco.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import br.com.bb.banco.entity.ClienteConta;
import jakarta.persistence.Tuple;


@Repository
public interface ClienteContaRepository extends JpaRepository<ClienteConta, Long>{

    /**
     * Consulta que retorna o histórico geral de movimentações de uma conta específica.
     * Combina os registros de movimentações pessoais (depósitos e saques) com as 
     * movimentações entre clientes (transferências enviadas e recebidas).
     * 
     * A consulta une (UNION ALL) duas subconsultas:
     * 1. Movimentações pessoais da tabela historico_movimentacao_cliente
     * 2. Transferências entre clientes da tabela historico_movimentacao_entre_clientes
     * 
     * O resultado é ordenado por data e hora decrescente, considerando ambos os tipos
     * de movimentação através do COALESCE.
     * 
     * @param id ID da conta do cliente para buscar o histórico
     * @param pageable Objeto com informações de paginação
     * @return Page<Tuple> Página contendo os registros do histórico
     */
    @Query(
        value = "SELECT * FROM (" +
                    "SELECT " +
                        "hmc.id_movimentacao AS mov_cliente_id, " +
                        "hmc.deposito AS deposito_cliente, " +
                        "hmc.saque AS saque_cliente, " +
                        "hmc.valor AS valor_cliente, " +
                        "hmc.data AS data_cliente, " +
                        "hmc.hora AS hora_cliente, " +
                        "hmc.id_conta AS id_conta_cliente, " +
                        "NULL AS mov_entre_cliente_id, " +
                        "NULL AS valor_entre_cliente, " +
                        "NULL AS data_entre_cliente, " +
                        "NULL AS hora_entre_cliente, " +
                        "NULL AS conta_remetente_id, " +
                        "NULL AS conta_destinatario_id " +
                    "FROM historico_movimentacao_cliente hmc " +
                    "WHERE hmc.id_conta = :id " +
                    "UNION ALL " +
                    "SELECT " +
                        "NULL AS mov_cliente_id, " +
                        "NULL AS deposito_cliente, " +
                        "NULL AS saque_cliente, " +
                        "NULL AS valor_cliente, " +
                        "NULL AS data_cliente, " +
                        "NULL AS hora_cliente, " +
                        "NULL AS id_conta_cliente, " +
                        "hmec.id_movimentacao AS mov_entre_cliente_id, " +
                        "hmec.valor AS valor_entre_cliente, " +
                        "hmec.data AS data_entre_cliente, " +
                        "hmec.hora AS hora_entre_cliente, " +
                        "hmec.id_conta_remetente AS conta_remetente_id, " +
                        "hmec.id_conta_destinatario AS conta_destinatario_id " +
                    "FROM historico_movimentacao_entre_clientes hmec " +
                    "WHERE hmec.id_conta_remetente = :id OR hmec.id_conta_destinatario = :id" +
                ") AS historico " +
                "ORDER BY " +
                "COALESCE(data_cliente, data_entre_cliente) DESC, " +
                "COALESCE(hora_cliente, hora_entre_cliente) DESC",
        countQuery = "SELECT COUNT(*) FROM (" +
                        "SELECT hmc.id_movimentacao " +
                        "FROM historico_movimentacao_cliente hmc " +
                        "WHERE hmc.id_conta = :id " +
                        "UNION ALL " +
                        "SELECT hmec.id_movimentacao " +
                        "FROM historico_movimentacao_entre_clientes hmec " +
                        "WHERE hmec.id_conta_remetente = :id OR hmec.id_conta_destinatario = :id" +
                    ") AS historico",
        nativeQuery = true
    )
    Page<Tuple> findHistoricoGeralById(@Param("id") Long id, Pageable pageable);
    
    
    /**
     * Busca uma conta de cliente com base na agência e número da conta
     * @param agencia número da agência bancária
     * @param numeroDaConta número da conta bancária
     * @return Optional contendo a conta do cliente se encontrada, ou vazio se não existir
     */
    Optional<ClienteConta> findByAgenciaAndNumeroDaConta(String agencia, String numeroDaConta);

}