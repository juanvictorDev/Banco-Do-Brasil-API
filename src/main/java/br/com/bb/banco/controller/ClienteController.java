package br.com.bb.banco.controller;

import java.util.Map;
import org.springframework.context.annotation.Lazy;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import br.com.bb.banco.dto.ClienteContaDto;
import br.com.bb.banco.dto.ClienteDadosDto;
import br.com.bb.banco.dto.ClientePerfilDto;
import br.com.bb.banco.dto.HistoricoGeralDto;
import br.com.bb.banco.dto.LoginDto;
import br.com.bb.banco.dto.RespostaTransacaoDto;
import br.com.bb.banco.security.JwtUtils;
import br.com.bb.banco.service.ClienteService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
public class ClienteController {

    ClienteService clienteService;

    AuthenticationManager authenticationManager;

    JwtUtils jwtUtils;

    public ClienteController(ClienteService clienteService, @Lazy AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.clienteService = clienteService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    
    // -- {CLIENTE DADOS} --


    /**
     * [CLIENTE DADOS ESPECIFICO]
     * Retorna os dados de um cliente específico através do ID.
     * @param id ID do cliente que será buscado
     * @return ResponseEntity contendo os dados do cliente encapsulados em um EntityModel
     */
    @GetMapping("/cliente/{id}/dados")
    public ResponseEntity<EntityModel<ClienteDadosDto>> buscarClienteDados(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(clienteService.encontrarClienteDados(id));
    }
    
    /**
     * [TODOS OS CLIENTE DADOS]
     * Retorna uma lista paginada com os dados de todos os clientes cadastrados.
     * @param page Número da página a ser retornada (padrão: 0)
     * @param size Quantidade de registros por página (padrão: 10)
     * @return ResponseEntity contendo uma lista paginada de dados dos clientes encapsulados em um PagedModel
     */
    @GetMapping("/cliente/dados")
    public ResponseEntity<PagedModel<EntityModel<ClienteDadosDto>>> buscarClientesDados(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(clienteService.encontrarClientesDados(page, size));
    }


    // -- {PERFIL DO CLIENTE} --


    /**
     * [PERFIL DO CLIENTE ESPECIFICO]
     * Retorna o perfil de um cliente específico através do ID.
     * @param id ID do perfil que será buscado
     * @return ResponseEntity contendo os dados do perfil do cliente encapsulados em um EntityModel
     */
    @GetMapping("cliente/{id}/perfil")
    public ResponseEntity<EntityModel<ClientePerfilDto>> buscarClientePerfil(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(clienteService.encontrarClientePerfil(id));
    }
    
    /**
     * [TODOS OS PERFIS DE CLIENTE]
     * Retorna uma lista paginada com os perfis de todos os clientes cadastrados.
     * @param page Número da página a ser retornada (padrão: 0)
     * @param size Quantidade de registros por página (padrão: 10)
     * @return ResponseEntity contendo uma lista paginada de perfis de clientes encapsulados em um PagedModel
     */
    @GetMapping("cliente/perfil")
    public ResponseEntity<PagedModel<EntityModel<ClientePerfilDto>>> buscarClientesPerfis(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(clienteService.encontrarClientesPerfis(page, size));
    }
    
    
    // -- {CONTA E OPERAÇÕES} --


    /**
     * [CLIENTE CONTA ESPECIFICA]
     * Retorna a conta de um cliente específico através do ID da conta.
     * @param id ID da conta que será buscada
     * @return ResponseEntity contendo os dados da conta do cliente encapsulados em um EntityModel
     */
    @GetMapping("/cliente/{id}/conta")
    public ResponseEntity<EntityModel<ClienteContaDto>> buscarClienteConta(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(clienteService.encontrarClienteConta(id));
    }

    /**
     * [DEPOSITO]
     * Realiza um depósito na conta do cliente.
     * @param id ID da conta que receberá o depósito
     * @param valor Valor a ser depositado na conta
     * @return ResponseEntity contendo os dados da transação encapsulados em um EntityModel
     */
    @PostMapping("/cliente/{id}/conta/deposito")
    public ResponseEntity<EntityModel<RespostaTransacaoDto>> depositarClienteConta(@PathVariable("id") Long id, @RequestParam("valor") Double valor) {
        return ResponseEntity.ok().body(clienteService.depositarNaConta(id, valor));
    }
    
    /**
     * [SAQUE]
     * Realiza um saque na conta do cliente.
     * @param id ID da conta que realizará o saque
     * @param valor Valor a ser sacado da conta
     * @return ResponseEntity contendo os dados da transação encapsulados em um EntityModel
     */
    @PostMapping("/cliente/{id}/conta/saque")
    public ResponseEntity<EntityModel<RespostaTransacaoDto>> sacarClienteConta(@PathVariable("id") Long id, @RequestParam("valor") Double valor) {
        return ResponseEntity.ok().body(clienteService.sacarDaConta(id, valor));
    }

    /**
     * [TRANSFERENCIA ENTRE CLIENTES]
     * Realiza uma transferência entre contas de clientes.
     * @param id ID da conta remetente
     * @param valor Valor a ser transferido
     * @param agencia Número da agência do destinatário
     * @param conta Número da conta do destinatário
     * @return ResponseEntity contendo os dados da transação encapsulados em um EntityModel
     */
    @PostMapping("/cliente/{id}/conta/transferencia")
    public ResponseEntity<EntityModel<RespostaTransacaoDto>> transferenciaEntreClienteConta(
        @PathVariable("id") Long id,
        @RequestParam("valor") Double valor,
        @RequestParam("agencia") String agencia, 
        @RequestParam("conta") String conta
    ) {
        return ResponseEntity.ok().body(clienteService.transferirValorEntreClientes(id, valor, agencia, conta));
    }
    
    /**
     * [HISTORICO GERAL DO CLIENTE]
     * Retorna o histórico de todas as movimentações da conta do cliente de forma paginada.
     * @param id ID da conta para buscar o histórico
     * @param page Número da página desejada (padrão: 0)
     * @param size Quantidade de itens por página (padrão: 10)
     * @return ResponseEntity contendo o histórico paginado das transações
     */
    @GetMapping("/cliente/{id}/conta/historico")
    public ResponseEntity<PagedModel<HistoricoGeralDto>> historicoClienteConta(
        @PathVariable Long id,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ){
        return ResponseEntity.ok().body(clienteService.buscarHistoricoGeral(id, page, size));
    }


    // -- {CADASTRO LOGIN E LOGOUT} --


    /**
     * [LOGIN]
     * Faz o login do cliente e retorna um cookie httpOnly com o token JWT.
     * @param login Objeto contendo os dados necessários para autenticação do cliente
     * @param response O objeto de resposta HTTP, com objetivo de adicionar o cookie JWT
     * @return ResponseEntity contendo uma mensagem de sucesso após a autenticação
     */
    @PostMapping("/cliente/login")
    public ResponseEntity<Map<String, String>> loginCliente(@RequestBody LoginDto login, HttpServletResponse response) {
        String jwt = clienteService.logarClienteERetornarJwt(login);
        return ResponseEntity.ok(Map.of("token", jwt, "message", "Autenticado com sucesso"));
    }

    /**
     * [LOGOUT]
     * Faz o logout do cliente e invalida o cookie JWT.
     * @param response O objeto de resposta HTTP, com objetico de adicionar um cookie invalido.
     * @return Um ResponseEntity com uma mensagem de sucesso.
     */
    @PostMapping("/cliente/logout")
    public ResponseEntity<String> logoutCliente(HttpServletResponse response) {
        response.addCookie(clienteService.logoutClienteInvalidarCookie());
        return ResponseEntity.ok("Logout realizado com sucesso");
    }

    /**
     * [CADASTRO]
     * Cadastra um novo cliente no sistema.
     * @param requestBody Objeto contendo os dados necessários para o cadastro do cliente
     * @return ResponseEntity contendo os dados do cliente cadastrado encapsulados em um EntityModel
     */
    @PostMapping("/cliente/cadastro") 
    public ResponseEntity<EntityModel<ClienteDadosDto>> cadastrarCliente(@RequestBody ClienteDadosDto requestBody){
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.cadastrarNovoCliente(requestBody));
    }
       
}
