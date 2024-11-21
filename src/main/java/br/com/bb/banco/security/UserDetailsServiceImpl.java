package br.com.bb.banco.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import br.com.bb.banco.entity.ClienteConta;
import br.com.bb.banco.repository.ClienteContaRepository;


@Service
public class UserDetailsServiceImpl implements UserDetailsService{

    ClienteContaRepository ClienteContaRepository;

    public UserDetailsServiceImpl(ClienteContaRepository clienteContaRepository) {
        ClienteContaRepository = clienteContaRepository;
    }

    
    public UserDetails loadUserByAgenciaAndNumeroDaConta(String agencia, String numeroDaConta){
        ClienteConta clienteConta = ClienteContaRepository.findByAgenciaAndNumeroDaConta(agencia, numeroDaConta)
        .orElseThrow(() -> new UsernameNotFoundException("Cliente não encontrado"));

        return new UserDetailsImpl(clienteConta);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UnsupportedOperationException("Use loadUserByAgenciaAndNumeroDaConta para autenticação");
    }

}