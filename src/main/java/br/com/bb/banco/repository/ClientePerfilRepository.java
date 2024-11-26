package br.com.bb.banco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import br.com.bb.banco.entity.ClientePerfil;
import java.util.Optional;

@Repository
public interface ClientePerfilRepository extends JpaRepository<ClientePerfil, Long>{
    
    /**
     * Seleciona o perfil do cliente com base no id do cliente.
     * @param id O identificador do cliente
     * @return Optional contendo o perfil do cliente se encontrado, ou vazio caso contrário
     */
    @Query(value = "SELECT * FROM cliente_perfil WHERE id_cliente = :id", nativeQuery = true)
    Optional<ClientePerfil> findByIdCliente(@Param("id") Long id);

}
