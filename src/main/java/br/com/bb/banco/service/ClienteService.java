package br.com.bb.banco.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.bb.banco.cliente.CriarPerfil;
import br.com.bb.banco.controller.ClienteController;
import br.com.bb.banco.dto.ClienteContaDto;
import br.com.bb.banco.dto.ClienteDadosDto;
import br.com.bb.banco.dto.ClientePerfilDto;
import br.com.bb.banco.dto.HistoricoGeralDto;
import br.com.bb.banco.dto.LoginDto;
import br.com.bb.banco.dto.RespostaTransacaoDto;
import br.com.bb.banco.entity.ClienteConta;
import br.com.bb.banco.entity.ClienteDados;
import br.com.bb.banco.entity.ClientePerfil;
import br.com.bb.banco.entity.HistoricoMovimentacaoCliente;
import br.com.bb.banco.entity.HistoricoMovimentacaoEntreClientes;
import br.com.bb.banco.repository.ClienteContaRepository;
import br.com.bb.banco.repository.ClienteDadosRepository;
import br.com.bb.banco.repository.ClientePerfilRepository;
import br.com.bb.banco.repository.HistoricoMovimentacaoClienteRepository;
import br.com.bb.banco.repository.HistoricoMovimentacaoEntreClientesRepository;
import br.com.bb.banco.security.CustomAuthenticationToken;
import br.com.bb.banco.security.JwtUtils;
import br.com.bb.banco.security.UserDetailsImpl;
import br.com.bb.banco.utils.ConversorDeObjetos;
import jakarta.persistence.Tuple;
import jakarta.servlet.http.Cookie;


@Service
public class ClienteService {
    
    ClienteDadosRepository clienteDadosRepository;

    ClientePerfilRepository clientePerfilRepository;

    ClienteContaRepository clienteContaRepository;

    HistoricoMovimentacaoClienteRepository historicoMovimentacaoClienteRepository;

    HistoricoMovimentacaoEntreClientesRepository historicoMovimentacaoEntreClientesRepository;

    ConversorDeObjetos conversorDeObjetos;

    CriarPerfil criarPerfil;
    
    AuthenticationManager authenticationManager;
    
    JwtUtils jwtUtils;

      
    public ClienteService(
        ClienteDadosRepository clienteDadosRepository,
        ClientePerfilRepository clientePerfilRepository,
        ClienteContaRepository clienteContaRepository, 
        HistoricoMovimentacaoClienteRepository historicoMovimentacaoClienteRepository,
        HistoricoMovimentacaoEntreClientesRepository historicoMovimentacaoEntreClientesRepository,
        ConversorDeObjetos conversorDeObjetos, CriarPerfil criarPerfil,
        AuthenticationManager authenticationManager, JwtUtils jwtUtils
    ) {
        this.clienteDadosRepository = clienteDadosRepository;
        this.clientePerfilRepository = clientePerfilRepository;
        this.clienteContaRepository = clienteContaRepository;
        this.historicoMovimentacaoClienteRepository = historicoMovimentacaoClienteRepository;
        this.historicoMovimentacaoEntreClientesRepository = historicoMovimentacaoEntreClientesRepository;
        this.conversorDeObjetos = conversorDeObjetos;
        this.criarPerfil = criarPerfil;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

        
    
    // -- {CLIENTE DADOS} --

    /**
     * [CLIENTE DADOS]
     * Metodo que retorna dados de um cliente específico
     * @param id Identificador único do cliente (idCliente)
     * @return EntityModel contendo os dados do cliente (ClienteDadosDto) e links HATEOAS relacionados
     * @throws NoSuchElementException quando o cliente não é encontrado
     */
    public EntityModel<ClienteDadosDto> encontrarClienteDados(Long id){
        
        ClienteDados clienteDados = clienteDadosRepository.findById(id)
        .orElseThrow(()-> new NoSuchElementException("Dados do cliente não exite"));

        clienteDados.setSenha(null);

        Link selfLink = linkTo(methodOn(ClienteController.class).buscarClienteDados(clienteDados.getIdCliente())).withSelfRel().withType("GET");
        Link perfilLink = linkTo(methodOn(ClienteController.class).buscarClientePerfil(clienteDados.getClientePerfil().getIdPerfil())).withRel("perfil").withType("GET");
        Link contaLink = linkTo(methodOn(ClienteController.class).buscarClienteConta(clienteDados.getClienteConta().getIdConta())).withRel("conta").withType("GET");
        
        return EntityModel.of(conversorDeObjetos.clienteDadosEntityParaDto(clienteDados), selfLink, perfilLink, contaLink);
    }

    /**
     * [TODOS CLIENTES DADOS]
     * Metodo que retorna todos os dados dos clientes de forma paginada
     * @param page Número da página desejada (começando em 0)
     * @param size Quantidade de registros por página
     * @return PagedModel contendo lista de EntityModel com dados dos clientes (ClienteDadosDto) e links HATEOAS relacionados
     * incluindo navegação entre páginas (self, next, previous, firstPage, lastPage)
     */
    public PagedModel<EntityModel<ClienteDadosDto>> encontrarClientesDados(Integer page, Integer size){
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ClienteDados> paginaDeDados = clienteDadosRepository.findAll(pageable);

        List<EntityModel<ClienteDadosDto>> entityModelList = paginaDeDados.getContent().stream()
        .map((dados) -> {
            dados.setSenha(null);
            ClienteDadosDto dto = conversorDeObjetos.clienteDadosEntityParaDto(dados);

            Link self = linkTo(methodOn(ClienteController.class).buscarClienteDados(dados.getIdCliente())).withSelfRel().withType("GET");
            Link perfil = linkTo(methodOn(ClienteController.class).buscarClientePerfil(dados.getClientePerfil().getIdPerfil())).withRel("perfil").withType("GET");
            Link conta = linkTo(methodOn(ClienteController.class).buscarClienteConta(dados.getClienteConta().getIdConta())).withRel("conta").withType("GET");
            
            return EntityModel.of(dto, self, perfil, conta);
        }).collect(Collectors.toList());

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(
            pageable.getPageSize(),
            paginaDeDados.getNumber(),
            paginaDeDados.getTotalElements()
        );
        
        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(ClienteController.class).buscarClientesDados(page, size)).withSelfRel().withType("GET"));
        
        if (!paginaDeDados.isLast() && paginaDeDados.hasNext() && paginaDeDados.hasContent()) {
            links.add(linkTo(methodOn(ClienteController.class).buscarClientesDados(page + 1, size)).withRel("next").withType("GET"));
        }
        
        if (!paginaDeDados.isFirst() && paginaDeDados.hasPrevious() && paginaDeDados.hasContent()) {
            links.add(linkTo(methodOn(ClienteController.class).buscarClientesDados(page - 1, size)).withRel("previous").withType("GET"));
        }
        
        if (!paginaDeDados.isFirst() && paginaDeDados.hasPrevious() && paginaDeDados.hasContent()) {
            links.add(linkTo(methodOn(ClienteController.class).buscarClientesDados(0, size)).withRel("firstPage").withType("GET"));
        }
        
        if (!paginaDeDados.isLast() && paginaDeDados.hasNext() && paginaDeDados.hasContent()) {
            links.add(linkTo(methodOn(ClienteController.class).buscarClientesDados(paginaDeDados.getTotalPages() - 1, size)).withRel("lastPage").withType("GET"));
        }

        return PagedModel.of(entityModelList, pageMetadata, links);
    }

    // -- {PERFIL DO CLIENTE} --


    /**
     * [PERFIL CLIENTE]
     * Método que retorna o perfil do cliente
     * @param id ID do perfil do cliente (idPerfil)
     * @return EntityModel contendo dados do perfil do cliente (ClientePerfilDto) e links HATEOAS relacionados (self, dados, conta)
     * @throws NoSuchElementException se o perfil não existir
     */
    public EntityModel<ClientePerfilDto> encontrarClientePerfil(Long id){
        
        ClientePerfil clientePerfil = clientePerfilRepository.findByIdCliente(id)
        .orElseThrow(() -> new NoSuchElementException("Esse perfil não existe"));

        Link selfLink = linkTo(methodOn(ClienteController.class).buscarClientePerfil(id)).withSelfRel();
        Link dados = linkTo(methodOn(ClienteController.class).buscarClienteDados(clientePerfil.getClienteDados().getIdCliente())).withRel("dados").withType("GET");
        Link conta = linkTo(methodOn(ClienteController.class).buscarClienteConta(clientePerfil.getClienteDados().getClienteConta().getIdConta())).withRel("conta").withType("GET");

        return EntityModel.of(conversorDeObjetos.clientePerfilEntityParaDto(clientePerfil), selfLink, dados, conta);
    }

    /**
     * [TODOS OS PERFIS]
     * Método que retorna todos os perfis de clientes de forma paginada
     * @param page número da página desejada (começa em 0)
     * @param size quantidade de elementos por página
     * @return PagedModel contendo lista de EntityModel com dados dos perfis (ClientePerfilDto) e links HATEOAS relacionados
     * incluindo navegação entre páginas (self, next, previous, firstPage, lastPage)
     */
    public PagedModel<EntityModel<ClientePerfilDto>> encontrarClientesPerfis(Integer page, Integer size){

        Pageable pageable = PageRequest.of(page, size);
        Page<ClientePerfil> paginaDePerfis = clientePerfilRepository.findAll(pageable);

        List<EntityModel<ClientePerfilDto>> entityModelList = paginaDePerfis.getContent().stream()
        .map((perfil) -> {
            ClientePerfilDto dto = conversorDeObjetos.clientePerfilEntityParaDto(perfil);

            Link self = linkTo(methodOn(ClienteController.class).buscarClientePerfil(dto.idPerfil())).withSelfRel().withType("GET");
            Link dados = linkTo(methodOn(ClienteController.class).buscarClienteDados(dto.idCliente())).withRel("dados").withType("GET");
            Link conta = linkTo(methodOn(ClienteController.class).buscarClienteConta(perfil.getClienteDados().getClienteConta().getIdConta())).withRel("conta").withType("GET");
            
            return EntityModel.of(dto, self, dados, conta);
        }).collect(Collectors.toList());

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(
            pageable.getPageSize(),
            paginaDePerfis.getNumber(),
            paginaDePerfis.getTotalElements()
        );

        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(ClienteController.class).buscarClientesPerfis(page, size)).withSelfRel().withType("GET"));
        
        if (!paginaDePerfis.isLast() && paginaDePerfis.hasNext() && paginaDePerfis.hasContent()) {
            links.add(linkTo(methodOn(ClienteController.class).buscarClientesPerfis(page + 1, size)).withRel("next").withType("GET"));
        }
        
        if (!paginaDePerfis.isFirst() && paginaDePerfis.hasPrevious() && paginaDePerfis.hasContent()) {
            links.add(linkTo(methodOn(ClienteController.class).buscarClientesPerfis(page - 1, size)).withRel("previous").withType("GET"));
        }
        
        if (!paginaDePerfis.isFirst() && paginaDePerfis.hasPrevious() && paginaDePerfis.hasContent()) {
            links.add(linkTo(methodOn(ClienteController.class).buscarClientesPerfis(0, size)).withRel("firstPage").withType("GET"));
        }
        
        if (!paginaDePerfis.isLast() && paginaDePerfis.hasNext() && paginaDePerfis.hasContent()) {
            links.add(linkTo(methodOn(ClienteController.class).buscarClientesPerfis(paginaDePerfis.getTotalPages() - 1, size)).withRel("lastPage").withType("GET"));
        }

        return PagedModel.of(entityModelList, pageMetadata, links);
    }

    
    // -- {CONTA E OPERAÇÕES} --

    /**
     * [CLIENTE CONTA]
     * Método para buscar conta do cliente
     * @param id ID da ClienteConta que será buscada (idConta)
     * @return EntityModel contendo dados da conta (ClienteContaDto) e links HATEOAS relacionados (self, deposito, saque, transferencia)
     * @throws RuntimeException se o ID enviado não for compatível com o ID do usuário autenticado
     * @throws NoSuchElementException se a conta não existir
     */
    public EntityModel<ClienteContaDto> encontrarClienteConta(Long id){
    
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    
        if(user.getClienteConta().getIdConta() != id){
            throw new RuntimeException("o id enviado não é compativel com o id autenticado");
        }

        ClienteConta clienteConta = clienteContaRepository.findById(id).
        orElseThrow(() -> new NoSuchElementException("Essa conta não existe"));
    
        ClienteContaDto clienteContaDto = ClienteContaDto.builder()
        .idConta(clienteConta.getIdConta())
        .agencia(clienteConta.getAgencia())
        .numeroDaConta(clienteConta.getNumeroDaConta())
        .saldo(clienteConta.getSaldo().doubleValue())
        .build();
    

        Link self = linkTo(methodOn(ClienteController.class).buscarClienteConta(id)).withSelfRel().withType("GET");
        Link deposito = linkTo(methodOn(ClienteController.class).depositarClienteConta(id, null)).withRel("deposito").withType("POST");
        Link saque = linkTo(methodOn(ClienteController.class).sacarClienteConta(id, null)).withRel("saque").withType("POST");
        Link transferencia = linkTo(methodOn(ClienteController.class).transferenciaEntreClienteConta(id, null, null, null)).withRel("transferencia").withType("POST");

        return EntityModel.of(clienteContaDto, self, deposito, saque, transferencia);
    }



    /**
     * [DEPOSITO]
     * Método para realizar depósito na conta do cliente e salvar no histórico
     * @param id ID da ClienteConta que receberá o depósito (idConta)
     * @param valor Valor a ser depositado (deve ser entre R$10,00 e R$10.000,00)
     * @return EntityModel contendo dados da transação (RespostaTransacaoDto) e links HATEOAS relacionados (self, saque, transferencia, conta)
     * @throws RuntimeException se o ID enviado não for compatível com o ID do usuário autenticado
     * @throws IllegalArgumentException se o valor do depósito for menor que R$10,00 ou maior que R$10.000,00
     * @throws NoSuchElementException se a conta não existir
     */
    @Transactional
    public EntityModel<RespostaTransacaoDto> depositarNaConta(Long id, Double valor){

        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    
        if(user.getClienteConta().getIdConta() != id){
            throw new RuntimeException("o id enviado não é compativel com o id autenticado");
        }

        if(valor < 10 || valor > 10000 || valor == null){
            throw new IllegalArgumentException("O valor minimo para deposito é de R$10,00 e o maximo é de R$10.000,00");
        }

        ClienteConta conta = clienteContaRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Essa conta não existe"));
    
        BigDecimal novoSaldo = conta.getSaldo().add(new BigDecimal(valor.toString()));
    
        RespostaTransacaoDto respostaTransacaoDto = RespostaTransacaoDto.builder()
        .agencia(conta.getAgencia())
        .conta(conta.getNumeroDaConta())
        .valorDepositado(valor)
        .saldoAnterior(conta.getSaldo().doubleValue())
        .saldoAtual(novoSaldo.doubleValue())
        .build();
    
        conta.setSaldo(novoSaldo);
    
        clienteContaRepository.save(conta);

        HistoricoMovimentacaoCliente historico = HistoricoMovimentacaoCliente.builder()
        .deposito(true)
        .saque(false)
        .valor(new BigDecimal(valor))
        .data(LocalDate.now())
        .hora(OffsetTime.now(ZoneOffset.of("-03:00")).format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        .clienteConta(conta)
        .build();

        historicoMovimentacaoClienteRepository.save(historico);

        Link self = linkTo(methodOn(ClienteController.class).depositarClienteConta(id, valor)).withSelfRel().withType("POST");
        Link saque = linkTo(methodOn(ClienteController.class).sacarClienteConta(id, null)).withRel("saque").withType("POST");
        Link transferencia = linkTo(methodOn(ClienteController.class).transferenciaEntreClienteConta(id, null, null, null)).withRel("transferencia").withType("POST");
        Link contaCliente = linkTo(methodOn(ClienteController.class).buscarClienteConta(id)).withRel("conta").withType("GET");
    
        return EntityModel.of(respostaTransacaoDto, self, saque, transferencia, contaCliente);
    }
    
    /**
     * [SAQUE]
     * Método para realizar o saque na conta do cliente e salvar no histórico de movimentações
     * @param id Identificador único da conta do cliente
     * @param valor Valor a ser sacado da conta
     * @return EntityModel<RespostaTransacaoDto> contendo os dados da transação e links HATEOAS
     * @throws RuntimeException se o id enviado não for compatível com o usuário autenticado
     * @throws NoSuchElementException se a conta não existir
     * @throws IllegalArgumentException se o valor for menor que R$10,00 ou maior que o saldo disponível
     */
    @Transactional
    public EntityModel<RespostaTransacaoDto> sacarDaConta(Long id, Double valor){

        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    
        if(user.getClienteConta().getIdConta() != id){
            throw new RuntimeException("o id enviado não é compativel com o id autenticado");
        }
            
        ClienteConta conta = clienteContaRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Essa conta não existe"));
    
        if(valor < 10){
            throw new IllegalArgumentException("O valor do saque deve ser maior que R$10,00");
        }
        if(valor > conta.getSaldo().doubleValue()){
            throw new IllegalArgumentException("O valor do saque ultrapassa o saldo disponivel");
        }
    
        BigDecimal novoSaldo = conta.getSaldo().subtract(new BigDecimal(valor.toString()));

        RespostaTransacaoDto respostaTransacaoDto = RespostaTransacaoDto.builder()
        .agencia(conta.getAgencia())
        .conta(conta.getNumeroDaConta())
        .valorSacado(valor)
        .saldoAnterior(conta.getSaldo().doubleValue())
        .saldoAtual(novoSaldo.doubleValue())
        .build();

        conta.setSaldo(novoSaldo);

        clienteContaRepository.save(conta);

        HistoricoMovimentacaoCliente historico = HistoricoMovimentacaoCliente.builder()
        .deposito(false)
        .saque(true)
        .valor(new BigDecimal(valor))
        .data(LocalDate.now())
        .hora(OffsetTime.now(ZoneOffset.of("-03:00")).format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        .clienteConta(conta)
        .build();

        historicoMovimentacaoClienteRepository.save(historico);

        Link self = linkTo(methodOn(ClienteController.class).sacarClienteConta(id, null)).withSelfRel().withType("POST");
        Link deposito = linkTo(methodOn(ClienteController.class).depositarClienteConta(id, valor)).withRel("deposito").withType("POST");
        Link transferencia = linkTo(methodOn(ClienteController.class).transferenciaEntreClienteConta(id, null, null, null)).withRel("transferencia").withType("POST");
        Link contaCliente = linkTo(methodOn(ClienteController.class).buscarClienteConta(id)).withRel("conta").withType("GET");
    
        return EntityModel.of(respostaTransacaoDto, self, deposito, transferencia, contaCliente);
    }

    /**
     * [TRANSFERENCIA ENTRE CLIENTES]
     * Realiza transferência de valores entre contas de clientes e registra no histórico
     * @param id ID da conta do cliente remetente
     * @param valor Valor a ser transferido
     * @param agencia Número da agência do destinatário
     * @param conta Número da conta do destinatário
     * @return EntityModel contendo os dados da transação e links HATEOAS
     * @throws RuntimeException se o ID não corresponder ao usuário autenticado ou tentar transferir para própria conta
     * @throws NoSuchElementException se a conta remetente ou destinatário não existir
     * @throws IllegalArgumentException se o valor for menor que R$10 ou maior que o saldo disponível
     */
    @Transactional
    public EntityModel<RespostaTransacaoDto> transferirValorEntreClientes(Long id, Double valor, String agencia, String conta){

        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    
        if(user.getClienteConta().getIdConta() != id){
            throw new RuntimeException("o id enviado não é compativel com o id autenticado");
        }

        if(agencia.equals(user.getClienteConta().getAgencia()) || conta.equals(user.getClienteConta().getNumeroDaConta())){
            throw new RuntimeException("você não pode fazer transferencia para sua propria conta");
        }


        ClienteConta contaRemetente = clienteContaRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Essa conta não existe"));

        ClienteConta contaDestinatario = clienteContaRepository.findByAgenciaAndNumeroDaConta(agencia, conta)
        .orElseThrow(() -> new NoSuchElementException("Essa conta não existe"));

        if(valor < 10){
            throw new IllegalArgumentException("O valor da transferencia deve ser maior que R$10,00");
        }

        if(valor > contaRemetente.getSaldo().doubleValue()){
            throw new IllegalArgumentException("O valor da transferencia ultrapassa o saldo disponivel");
        }

        BigDecimal novoSaldoRemetente =  contaRemetente.getSaldo().subtract(new BigDecimal(valor.toString()));
        BigDecimal novoSaldoDestinatario = contaDestinatario.getSaldo().add(new BigDecimal(valor.toString()));

        RespostaTransacaoDto respostaTransacaoDto = RespostaTransacaoDto.builder()
        .agencia(contaRemetente.getAgencia())
        .conta(contaRemetente.getNumeroDaConta())
        .valorTransferido(valor)
        .saldoAnterior(contaRemetente.getSaldo().doubleValue())
        .saldoAtual(novoSaldoRemetente.doubleValue())
        .agenciaDestinatario(contaDestinatario.getAgencia())
        .contaDestinatario(contaDestinatario.getNumeroDaConta())
        .build();
    
        contaRemetente.setSaldo(novoSaldoRemetente);
        contaDestinatario.setSaldo(novoSaldoDestinatario);
    
        clienteContaRepository.save(contaRemetente);
        clienteContaRepository.save(contaDestinatario);

        HistoricoMovimentacaoEntreClientes historico = HistoricoMovimentacaoEntreClientes.builder()
        .valor(new BigDecimal(valor.toString()))
        .data(LocalDate.now())
        .hora(OffsetTime.now(ZoneOffset.of("-03:00")).format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        .clienteContaRemetente(contaRemetente)
        .clienteContaDestinatario(contaDestinatario)
        .build();

        historicoMovimentacaoEntreClientesRepository.save(historico);

        Link self = linkTo(methodOn(ClienteController.class).transferenciaEntreClienteConta(id, valor, agencia, conta)).withSelfRel().withType("POST");
        Link deposito = linkTo(methodOn(ClienteController.class).depositarClienteConta(id, valor)).withRel("deposito").withType("POST");
        Link saque = linkTo(methodOn(ClienteController.class).sacarClienteConta(id, null)).withRel("saque").withType("POST");
        Link contaCliente = linkTo(methodOn(ClienteController.class).buscarClienteConta(id)).withRel("conta").withType("GET");
    
        return EntityModel.of(respostaTransacaoDto, self, deposito, saque, contaCliente);
    }    

    /**
     * [HISTORICO GERAL]
     * Busca o histórico geral da conta do cliente de forma paginada
     * @param id ID da conta do cliente
     * @param page Número da página desejada (começa em 0)
     * @param size Quantidade de registros por página
     * @return PagedModel contendo lista de HistoricoGeralDto com links HATEOAS
     * @throws RuntimeException se o ID enviado não for compatível com o usuário autenticado
     */
    public PagedModel<HistoricoGeralDto> buscarHistoricoGeral(Long id, Integer page, Integer size){
        
        UserDetailsImpl user = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if(user.getClienteConta().getIdConta() != id){
            throw new RuntimeException("o id enviado não é compativel com o id autenticado");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Tuple> paginaHistorico = clienteContaRepository.findHistoricoGeralById(id, pageable);

        List<HistoricoGeralDto> listaHistorico = paginaHistorico.stream()
        .map((tuple) -> conversorDeObjetos.tupleParaHistoricoGeralDto(tuple))
        .collect(Collectors.toList());

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(
            pageable.getPageSize(),
            paginaHistorico.getNumber(),
            paginaHistorico.getTotalElements()
        );

        List<Link> links = new ArrayList<>();
        links.add(linkTo(methodOn(ClienteController.class).historicoClienteConta(id, page, size)).withSelfRel().withType("GET"));
        
        if (!paginaHistorico.isLast() && paginaHistorico.hasNext() && paginaHistorico.hasContent()) {
            links.add(linkTo(methodOn(ClienteController.class).historicoClienteConta(id, page + 1, size)).withRel("next").withType("GET"));
        }
        
        if (!paginaHistorico.isFirst() && paginaHistorico.hasPrevious() && paginaHistorico.hasContent()) {
            links.add(linkTo(methodOn(ClienteController.class).historicoClienteConta(id, page - 1, size)).withRel("previous").withType("GET"));
        }
        
        if (!paginaHistorico.isFirst() && paginaHistorico.hasPrevious() && paginaHistorico.hasContent()) {
            links.add(linkTo(methodOn(ClienteController.class).historicoClienteConta(id, 0, size)).withRel("firstPage").withType("GET"));
        }
        
        if (!paginaHistorico.isLast() && paginaHistorico.hasNext() && paginaHistorico.hasContent()) {
            links.add(linkTo(methodOn(ClienteController.class).historicoClienteConta(id, paginaHistorico.getTotalPages() - 1, size)).withRel("lastPage").withType("GET"));
        }

        links.add(linkTo(methodOn(ClienteController.class).buscarClienteConta(id)).withRel("conta").withType("GET"));

        return PagedModel.of(listaHistorico, pageMetadata, links);
    }
     
    
    // -- {CADASTRO LOGIN E LOGOUT} --
        
    /**
     * Realiza a autenticação do cliente e gera um token JWT
     * @param login Objeto contendo agência, conta e senha do cliente
     * @return Token JWT gerado após autenticação bem-sucedida
     * @throws AuthenticationException se as credenciais forem inválidas
     */
    public String logarClienteERetornarJwt(LoginDto login){

        CustomAuthenticationToken customAuthenticationToken = new CustomAuthenticationToken(login.agencia(), login.conta(), login.senha());
            
        Authentication authentication =  authenticationManager.authenticate(customAuthenticationToken);
        
        String jwt = jwtUtils.gerarTokenJwt((UserDetailsImpl) authentication.getPrincipal());
        
        return jwt;
    }

    public Cookie logoutClienteInvalidarCookie(){
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);

        return jwtCookie;
    }

    public EntityModel<ClienteDadosDto> cadastrarNovoCliente(ClienteDadosDto requestBody){

        ClienteDados clienteDados = conversorDeObjetos.clienteDadosDtoParaEntity(requestBody);
        ClienteConta clienteConta = new ClienteConta();

        clienteDados.setClienteConta(clienteConta);
        clienteConta.setClienteDados(clienteDados);
    
        ClienteDados clienteDadosDatabase = clienteDadosRepository.save(clienteDados);

        criarPerfil.criar(clienteDadosDatabase);
        
        Link selfLink = linkTo(methodOn(ClienteController.class).buscarClienteDados(clienteDadosDatabase.getIdCliente())).withSelfRel().withType("GET");
        Link perfilLink = linkTo(methodOn(ClienteController.class).buscarClientePerfil(clienteDadosDatabase.getClientePerfil().getIdPerfil())).withRel("perfil").withType("GET");
        Link contaLink = linkTo(methodOn(ClienteController.class).buscarClienteConta(clienteDadosDatabase.getClienteConta().getIdConta())).withRel("conta").withType("GET");
        
        return EntityModel.of(conversorDeObjetos.clienteDadosEntityParaDto(clienteDadosDatabase), selfLink, contaLink, perfilLink);
    }
}
