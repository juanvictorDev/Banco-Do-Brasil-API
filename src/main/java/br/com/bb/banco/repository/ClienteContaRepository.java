package br.com.bb.banco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.bb.banco.entity.ClienteConta;

@Repository
public interface ClienteContaRepository extends JpaRepository<ClienteConta, Long>{
    
}
