package br.com.bb.banco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.bb.banco.entity.HistoricoMovimentacaoEntreClientes;

@Repository
public interface HistoricoMovimentacaoEntreClientesRepository extends JpaRepository<HistoricoMovimentacaoEntreClientes, Long>{
    
}
