package br.com.bb.banco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.bb.banco.entity.ClienteDados;

@Repository
public interface ClienteDadosRepository extends JpaRepository<ClienteDados, Long>{
    
}
