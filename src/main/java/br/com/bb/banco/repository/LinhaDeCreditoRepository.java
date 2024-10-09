package br.com.bb.banco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.bb.banco.entity.LinhaDeCredito;

@Repository
public interface LinhaDeCreditoRepository extends JpaRepository<LinhaDeCredito, Long>{
    
}
