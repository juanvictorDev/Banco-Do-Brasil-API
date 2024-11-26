package br.com.bb.banco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.com.bb.banco.entity.LinhaDeCredito;
import br.com.bb.banco.entity.types.TipoLinhaDeCredito;

@Repository
public interface LinhaDeCreditoRepository extends JpaRepository<LinhaDeCredito, Long>{
    
    /**
     * Busca uma linha de crédito pelo seu tipo específico
     * @param tipo O tipo de linha de crédito a ser buscado
     * @return A linha de crédito correspondente ao tipo informado
     */
    LinhaDeCredito findByTipo(TipoLinhaDeCredito tipo);

}
