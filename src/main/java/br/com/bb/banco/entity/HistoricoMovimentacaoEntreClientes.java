package br.com.bb.banco.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter @AllArgsConstructor
@NoArgsConstructor @Builder
@Entity @Table(name = "historico_movimentacao_entre_clientes")
public class HistoricoMovimentacaoEntreClientes {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long idMovimentacao;
    
    @Column(nullable = false)
    BigDecimal valor;

    @Column(nullable = false)
    LocalDate data;

    @Column(nullable = false)
    OffsetTime hora;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conta_remetente", nullable = false)
    ClienteConta clienteContaRemetente;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_conta_destinatario", nullable = false)
    ClienteConta clienteContaDestinatario;
}
