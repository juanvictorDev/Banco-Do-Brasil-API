package br.com.bb.banco.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import br.com.bb.banco.cliente.CriarPerfil;
import br.com.bb.banco.dto.ClienteDadosDto;
import br.com.bb.banco.dto.ClientePerfilDto;
import br.com.bb.banco.entity.ClienteConta;
import br.com.bb.banco.entity.ClienteDados;
import br.com.bb.banco.entity.ClientePerfil;
import br.com.bb.banco.entity.LinhaDeCredito;
import br.com.bb.banco.repository.ClienteDadosRepository;
import br.com.bb.banco.repository.ClientePerfilRepository;
import br.com.bb.banco.repository.LinhaDeCreditoRepository;
import br.com.bb.banco.utils.ConversorDeObjetos;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
public class ClienteController {

    @Autowired
    ClienteDadosRepository clienteDadosRepository;
    
    @Autowired
    ClientePerfilRepository clientePerfilRepository;
    
    @Autowired
    LinhaDeCreditoRepository linhaDeCreditoRepository;
    
    @Autowired
    ConversorDeObjetos conversorDeObjetos;
    
    @Autowired
    CriarPerfil criarPerfil;



    @PostMapping("/cliente") 
    ResponseEntity<Void> cadastrarCliente(@RequestBody ClienteDadosDto ClienteDadosDto){
        
        ClienteDados clienteDados = conversorDeObjetos.clienteDadosDtoParaEntity(ClienteDadosDto);
        ClienteConta clienteConta = new ClienteConta();

        clienteDados.setClienteConta(clienteConta);
        clienteConta.setIdCliente(clienteDados);

        clienteDadosRepository.save(clienteDados);

        return ResponseEntity.ok().build();
    }


    @GetMapping("/cliente/{id}")
    ResponseEntity<ClienteDadosDto> buscarCliente(@PathVariable(name = "id") Long id) {
        
        ClienteDados clienteDados = clienteDadosRepository.findById(id).get();
        clienteDados.setSenha(null);
        
        ClienteDadosDto dto = conversorDeObjetos.clienteDadosEntityParaDto(clienteDados);

        return ResponseEntity.ok().body(dto);
    }
    

    @PostMapping("/perfil/{id}")
    public ResponseEntity<Void> criarPerfil(@PathVariable(name = "id") Long id) {
        
        ClienteDados clienteDados = clienteDadosRepository.findById(id).get();
        criarPerfil.criar(clienteDados);
        
        return ResponseEntity.ok().build();
    }


    @GetMapping("/perfil/{id}")
    public ResponseEntity<ClientePerfilDto> buscarPerfil(@PathVariable(name = "id") Long id) {
        
        ClienteDados clienteDados = clienteDadosRepository.findById(id).get();
        ClientePerfil clientePerfil = clienteDados.getClientePerfil();
        ClientePerfilDto clientePerfilDto = conversorDeObjetos.ClientePerfilEntityParaDto(clientePerfil);

        return ResponseEntity.ok().body(clientePerfilDto);
    }
    

    @GetMapping("/creditos")
    public ResponseEntity<Map<String, List<LinhaDeCredito>>> buscarLinhasDeCredito() {
        List<LinhaDeCredito> linhasDeCredito = linhaDeCreditoRepository.findAll();

        List<LinhaDeCredito> antecipacao = new ArrayList<>();
        List<LinhaDeCredito> financiamento = new ArrayList<>();
        List<LinhaDeCredito> creditoNovo = new ArrayList<>();

        Map<String, List<LinhaDeCredito>> format = new HashMap<>();


        for (LinhaDeCredito linhaDeCredito : linhasDeCredito) {
            
            if(linhaDeCredito.getTipo().name().equals("ANTECIPACAO")){
                antecipacao.add(linhaDeCredito);
            }

            if(linhaDeCredito.getTipo().name().equals("FINANCIAMENTO")){
                financiamento.add(linhaDeCredito);
            }

            if(linhaDeCredito.getTipo().name().equals("CREDITO_NOVO")){
                creditoNovo.add(linhaDeCredito);
            }

        }
        
        format.put("financiamento", financiamento);
        format.put("antecipacao", antecipacao);
        format.put("credito novo", creditoNovo);


        return ResponseEntity.ok().body(format);
    }
    

}
