package br.com.bb.banco.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;


@Getter @Setter @NoArgsConstructor 
@AllArgsConstructor @Builder
@Entity @Table(name = "cliente_conta")
public class ClienteConta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long idConta;

    @Column(nullable = false)
    @ColumnDefault("0.00")
    BigDecimal saldo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    ClienteDados idCliente;

    @OneToMany(mappedBy = "idConta", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<HistoricoMovimentacaoCliente> movimentacaoCliente;

    @OneToMany(mappedBy = "idContaRemetente", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<HistoricoMovimentacaoEntreClientes> historicoMovimentacaoClienteRemetente;

    @OneToMany(mappedBy = "idContaDestinatario", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    List<HistoricoMovimentacaoEntreClientes> historicoMovimentacaoClienteDestinatario;
}
