package br.com.bb.banco.utils;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import br.com.bb.banco.dto.ClienteDadosDto;
import br.com.bb.banco.dto.ClientePerfilDto;
import br.com.bb.banco.dto.HistoricoGeralDto;
import br.com.bb.banco.dto.LinhaDeCreditoDto;
import br.com.bb.banco.entity.ClienteConta;
import br.com.bb.banco.entity.ClienteDados;
import br.com.bb.banco.entity.ClientePerfil;
import br.com.bb.banco.entity.LinhaDeCredito;
import br.com.bb.banco.entity.types.Escolaridade;
import br.com.bb.banco.entity.types.EstadoCivil;
import br.com.bb.banco.entity.types.Ocupacao;
import br.com.bb.banco.entity.types.PessoaComDeficiencia;
import br.com.bb.banco.entity.types.Sexo;
import br.com.bb.banco.repository.ClienteContaRepository;
import jakarta.persistence.Tuple;


@Component
public class ConversorDeObjetos {
    
    ClienteContaRepository clienteContaRepository;

    public ConversorDeObjetos(ClienteContaRepository clienteContaRepository) {
        this.clienteContaRepository = clienteContaRepository;
    }


    /**
     * @param dto objeto ClienteDadosDto contendo os dados do cliente
     * @return objeto ClienteDados com os dados convertidos
     */
    public ClienteDados clienteDadosDtoParaEntity(ClienteDadosDto dto){
        
        return ClienteDados.builder()
        .idCliente(dto.idCliente())
        .nome(dto.nome())
        .cpf(dto.cpf())
        .email(dto.email())
        .senha(dto.senha())
        .telefone(dto.telefone())
        .dataDeNascimento(dto.dataDeNascimento())
        .cep(dto.cep())
        .estado(dto.estado())
        .cidade(dto.cidade())
        .bairro(dto.bairro())
        .rua(dto.rua())
        .numeroResidencia(dto.numeroRua())
        .pcd(dto.pcd() != null ? PessoaComDeficiencia.valueOf(dto.pcd()) : null)
        .sexo(Sexo.valueOf(dto.sexo()))
        .escolaridade(Escolaridade.valueOf(dto.escolaridade()))
        .estadoCivil(EstadoCivil.valueOf(dto.estadoCivil()))
        .ocupacao(Ocupacao.valueOf(dto.ocupacao()))
        .rendaMensal(dto.rendaMensal())
        .build();

    }

    /**
     * @param entity objeto ClienteDados contendo os dados do cliente
     * @return objeto ClienteDadosDto com os dados convertidos
     */
    public ClienteDadosDto clienteDadosEntityParaDto(ClienteDados entity){
        
        return ClienteDadosDto.builder()
        .idCliente(entity.getIdCliente())
        .nome(entity.getNome())
        .cpf(entity.getCpf())
        .email(entity.getEmail())
        .senha(entity.getSenha())
        .telefone(entity.getTelefone())
        .dataDeNascimento(entity.getDataDeNascimento())
        .cep(entity.getCep())
        .estado(entity.getEstado())
        .cidade(entity.getCidade())
        .bairro(entity.getBairro())
        .rua(entity.getRua())
        .numeroRua(entity.getNumeroResidencia())
        .pcd(entity.getPcd() != null ? entity.getPcd().name() : null)
        .sexo(entity.getSexo().name())
        .escolaridade(entity.getEscolaridade().name())
        .estadoCivil(entity.getEstadoCivil().name())
        .ocupacao(entity.getOcupacao().name())
        .rendaMensal(entity.getRendaMensal())
        .build();

    }

    /**
     * @param entity objeto ClientePerfil contendo os dados do perfil do cliente
     * @return objeto ClientePerfilDto com os dados convertidos
     */
    public ClientePerfilDto clientePerfilEntityParaDto(ClientePerfil entity){

        return ClientePerfilDto.builder()
        .idPerfil(entity.getIdPerfil())
        .score(entity.getScore())
        .notaDoPerfil(entity.getNotaDoPerfil())
        .avaliacao(entity.getAvaliacao().name())
        .idCliente(entity.getClienteDados().getIdCliente())
        .build();

    }

    /**
     * @param entity objeto LinhaDeCredito contendo os dados da linha de crédito
     * @return objeto LinhaDeCreditoDto com os dados convertidos
     */
    public LinhaDeCreditoDto linhaDeCreditoEntityParaDto(LinhaDeCredito entity){

        return LinhaDeCreditoDto.builder()
        .idLinhaDeCredito(entity.getIdLinhaDeCredito())
        .nome(entity.getNome())
        .descricao(entity.getDescricao())
        .taxaDeJuros(entity.getTaxaDeJuros())
        .imagemNome(entity.getImagemNome())
        .linkSite(entity.getLinkSite())
        .tipo(entity.getTipo().name())
        .build();

    }

    /**
     * @param tuple objeto Tuple contendo os dados da movimentação
     * @return HistoricoGeralDto com os dados convertidos da movimentação
     * Se mov_entre_cliente_id não for nulo, retorna dados de movimentação entre clientes
     * Caso contrário, retorna dados de movimentação pessoal do cliente
     */
    public HistoricoGeralDto tupleParaHistoricoGeralDto(Tuple tuple){
        
        if(tuple.get("mov_entre_cliente_id") != null ){

            ClienteConta contaRemetente = clienteContaRepository.findById((Long)tuple.get("conta_remetente_id")).get();
            ClienteConta contaDestinatario = clienteContaRepository.findById((Long)tuple.get("conta_destinatario_id")).get();
            
            return HistoricoGeralDto.builder()
            .idMovimentacaoEntreClientes(tuple.get("mov_entre_cliente_id", Long.class))
            .valorEntreClientes(tuple.get("valor_entre_cliente", BigDecimal.class))
            .dataEntreClientes(sqlDateParaLocalDate(tuple.get("data_entre_cliente", Date.class)))
            .horaEntreClientes(tuple.get("hora_entre_cliente", String.class))
            .idContaRemetente(tuple.get("conta_remetente_id", Long.class))
            .idContaDestinatario(tuple.get("conta_destinatario_id", Long.class))
            .nomeRemetente(contaRemetente.getClienteDados().getNome())
            .agenciaRemetente(contaRemetente.getAgencia())
            .contaRemetente(contaRemetente.getNumeroDaConta())
            .nomeDestinatario(contaDestinatario.getClienteDados().getNome())
            .agenciaDestinatario(contaDestinatario.getAgencia())
            .contaDestinatario(contaDestinatario.getNumeroDaConta())
            .build();
        
        }else{
            
            return HistoricoGeralDto.builder()
            .idMovimentacaoCliente(tuple.get("mov_cliente_id", Long.class))
            .depositoCliente(tuple.get("deposito_cliente", Boolean.class))
            .saqueCliente(tuple.get("saque_cliente", Boolean.class))
            .valorCliente(tuple.get("valor_cliente", BigDecimal.class))
            .dataCliente(sqlDateParaLocalDate(tuple.get("data_cliente", Date.class)))
            .horaCliente(tuple.get("hora_cliente", String.class))
            .idContaCliente(tuple.get("id_conta_cliente", Long.class))
            .build();
        }

    }

    /**
     * Método auxiliar
     * @param sqlDate data em formato java.sql.Date a ser convertida
     * @return LocalDate convertido ou null se o parâmetro for null
     */
    private LocalDate sqlDateParaLocalDate(Date sqlDate) {
        return sqlDate != null ? sqlDate.toLocalDate() : null;
    }
}
