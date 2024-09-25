package br.com.bb.banco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.bb.banco.entity.ClientePerfil;

@Repository
public interface ClientePerfilRepository extends JpaRepository<ClientePerfil, Long>{
    
}
