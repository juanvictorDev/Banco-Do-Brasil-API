package br.com.bb.banco.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Builder @Getter @Setter
public class HistoricoGeralDto {

    // Campos de movimentação pessoal
    Long idMovimentacaoCliente;
    Boolean depositoCliente; 
    Boolean saqueCliente; 
    BigDecimal valorCliente; 
    LocalDate dataCliente; 
    String horaCliente; 
    Long idContaCliente; 
    
    // Campos de movimentação entre clientes
    Long idMovimentacaoEntreClientes; 
    BigDecimal valorEntreClientes; 
    LocalDate dataEntreClientes; 
    String horaEntreClientes; 
    Long idContaRemetente; 
    String nomeRemetente;
    String agenciaRemetente;
    String contaRemetente;
    Long idContaDestinatario;
    String nomeDestinatario;
    String agenciaDestinatario;
    String contaDestinatario;
}