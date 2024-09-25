package br.com.bb.banco.entity;

import br.com.bb.banco.entity.types.Avaliacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter @NoArgsConstructor
@AllArgsConstructor @Builder
@Entity @Table(name = "cliente_perfil")
public class ClientePerfil {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    Long idPerfil;

    @Column(nullable = false)
    Double notaDoPerfil;
    
    @Column(nullable = false)
    Double score;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Avaliacao avaliacao;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    ClienteDados idCliente;

}