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
    Long idConta;

    @Builder.Default
    @ColumnDefault(value = "0.00") @Column(nullable = false)
    BigDecimal saldo = BigDecimal.ZERO;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    ClienteDados clienteDados;

    @OneToMany(mappedBy = "clienteConta", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<HistoricoMovimentacaoCliente> movimentacaoCliente;

    @OneToMany(mappedBy = "clienteContaRemetente", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<HistoricoMovimentacaoEntreClientes> historicoMovimentacaoClienteRemetente;

    @OneToMany(mappedBy = "clienteContaDestinatario", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    List<HistoricoMovimentacaoEntreClientes> historicoMovimentacaoClienteDestinatario;
}
