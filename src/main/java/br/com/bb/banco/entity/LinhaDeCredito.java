package br.com.bb.banco.entity;

import br.com.bb.banco.entity.types.TipoLinhaDeCredito;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter @AllArgsConstructor
@NoArgsConstructor @Builder
@Entity @Table(name = "linha_de_credito")
public class LinhaDeCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idCredito;
    
    @Column(nullable = false, unique = true)
    String nome;

    @Column(nullable = false)
    String descricao;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    TipoLinhaDeCredito tipo;


}
