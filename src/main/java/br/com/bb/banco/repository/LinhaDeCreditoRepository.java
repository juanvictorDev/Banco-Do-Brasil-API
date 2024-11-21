package br.com.bb.banco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.bb.banco.entity.LinhaDeCredito;
import br.com.bb.banco.entity.types.TipoLinhaDeCredito;

@Repository
public interface LinhaDeCreditoRepository extends JpaRepository<LinhaDeCredito, Long>{
    
    // Select para retornar a linha de credito pelo seu tipo
    LinhaDeCredito findByTipo(TipoLinhaDeCredito tipo);

}
