package br.com.bb.banco.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import br.com.bb.banco.entity.ClienteConta;


public class UserDetailsImpl implements UserDetails{

    ClienteConta clienteConta;

    public UserDetailsImpl(ClienteConta clienteConta) {
        this.clienteConta = clienteConta;
    }

    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(clienteConta.getClienteDados().getRole().toString()));
    }

    @Override
    public String getPassword() {
        return clienteConta.getClienteDados().getSenha();
    }

    @Override
    public String getUsername() {
        return clienteConta.getClienteDados().getNome();
    }


    public ClienteConta getClienteConta() {
        return clienteConta;
    }

    public void setClienteConta(ClienteConta clienteConta) {
        this.clienteConta = clienteConta;
    }

}