package br.com.bb.banco.security;

import java.util.Collection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {
    
    String agencia;
    String conta;

    public CustomAuthenticationToken(String agencia, String conta, String senha) { 
        super(conta, senha); 
        this.agencia = agencia; 
        this.conta = conta; 
    }

    public CustomAuthenticationToken(UserDetails userDetails, String senha, Collection<? extends GrantedAuthority> authorities, String agencia, String conta){
        super(userDetails, senha, authorities);
        this.agencia = agencia; 
        this.conta = conta; 
    }

    public String getAgencia() {
        return agencia;
    }

    public String getConta() {
        return conta;
    }

}
