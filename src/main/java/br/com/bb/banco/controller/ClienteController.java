package br.com.bb.banco.controller;

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

    // [CLIENTE DADOS ESPECIFICO]
    // Retorna dados de um cliente especifico, recebe idCliente
    @GetMapping("/cliente/{id}/dados")
    public ResponseEntity<EntityModel<ClienteDadosDto>> buscarClienteDados(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(clienteService.encontrarClienteDados(id));
    }
    
    // [TODOS OS CLIENTE DADOS]
    // Retorna os dados de todos os clientes de forma paginada, recebe page e size opcionais
    @GetMapping("/cliente/dados")
    public ResponseEntity<PagedModel<EntityModel<ClienteDadosDto>>> buscarClientesDados(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(clienteService.encontrarClientesDados(page, size));
    }

    // -- {PERFIL DO CLIENTE} --

    // [PERFIL DO CLINTE ESPECIFICO]
    // Retorna o perfil de um cliente especifico, recebe idPerfil
    @GetMapping("cliente/{id}/perfil")
    public ResponseEntity<EntityModel<ClientePerfilDto>> buscarClientePerfil(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(clienteService.encontrarClientePerfil(id));
    }
    
    // [TODOS OS PERFIS DE CLIENTE]
    // Retorna os perfis de todos os clientes de forma paginada, recebe page e size opcionais
    @GetMapping("cliente/perfil")
    public ResponseEntity<PagedModel<EntityModel<ClientePerfilDto>>> buscarClientesPerfis(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        return ResponseEntity.ok().body(clienteService.encontrarClientesPerfis(page, size));
    }
    
    // -- {CONTA E OPERAÇÕES} --

    // [CLIENTE CONTA ESPECIFICA]
    // Retorna a conta de um cliente especifico, recebe idConta
    @GetMapping("/cliente/{id}/conta")
    public ResponseEntity<EntityModel<ClienteContaDto>> buscarClienteConta(@PathVariable("id") Long id) {
        return ResponseEntity.ok().body(clienteService.encontrarClienteConta(id));
    }

    // [DEPOSITO]
    // Deposita um valor na conta do cliente, recebe idConta na url e valor como parametro
    @PostMapping("/cliente/{id}/conta/deposito")
    public ResponseEntity<EntityModel<RespostaTransacaoDto>> depositarClienteConta(@PathVariable("id") Long id, @RequestParam("valor") Double valor) {
        return ResponseEntity.ok().body(clienteService.depositarNaConta(id, valor));
    }
    
    // [SAQUE]
    // Saca um valor da conta do cliente, recebe idConta na url e valor como paramentro
    @PostMapping("/cliente/{id}/conta/saque")
    public ResponseEntity<EntityModel<RespostaTransacaoDto>> sacarClienteConta(@PathVariable("id") Long id, @RequestParam("valor") Double valor) {
        return ResponseEntity.ok().body(clienteService.sacarDaConta(id, valor));
    }

    // [TRANSFERENCIA ENTRE CLIENTES]
    // Transfere o valor de uma conta para outra, recebe idConta do remetente na url, valor e agencia e conta do destinatario como parametros
    @PostMapping("/cliente/{id}/conta/transferencia")
    public ResponseEntity<EntityModel<RespostaTransacaoDto>> transferenciaEntreClienteConta(
        @PathVariable("id") Long id,
        @RequestParam("valor") Double valor,
        @RequestParam("agencia") String agencia, 
        @RequestParam("conta") String conta
    ) {
        return ResponseEntity.ok().body(clienteService.transferirValorEntreClientes(id, valor, agencia, conta));
    }
    
    // [HISTORICO GERAL DO CLIENTE]
    // Retorna o historico da conta do cliente com todas as movimentações feitas de forma pagina, recebe idConta
    @GetMapping("/cliente/{id}/conta/historico")
    public ResponseEntity<PagedModel<HistoricoGeralDto>> historicoClienteConta(
        @PathVariable Long id,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ){
        return ResponseEntity.ok().body(clienteService.buscarHistoricoGeral(id, page, size));
    }

    // -- {CADASTRO LOGIN E LOGOUT} --

    // [LOGIN]
    // Faz o login do cliente e retorna o token jwt, recebe um request body com dados necessarios para login
    @PostMapping("/login")
    public ResponseEntity<String> loginCliente(@RequestBody LoginDto login) {
        return ResponseEntity.ok().body(clienteService.logarClienteERetornarJwt(login));
    }

    // [CADASTRO]
    // Cadastra um novo cliente, recebe um request body com os dados necessarios para cadastro
    @PostMapping("/cadastro") 
    public ResponseEntity<EntityModel<ClienteDadosDto>> cadastrarCliente(@RequestBody ClienteDadosDto requestBody){
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.cadastrarNovoCliente(requestBody));
    }

}
