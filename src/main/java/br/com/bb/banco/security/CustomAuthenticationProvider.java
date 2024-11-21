package br.com.bb.banco.security;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;



@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsServiceImpl userDetailsService;

    private PasswordEncoder passwordEncoder;

    public CustomAuthenticationProvider(UserDetailsServiceImpl userDetailsService, @Lazy PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String agencia = ((CustomAuthenticationToken) authentication).getAgencia();
        String conta = ((CustomAuthenticationToken) authentication).getConta();
        String senha = (String) authentication.getCredentials();

        UserDetails userDetails = userDetailsService.loadUserByAgenciaAndNumeroDaConta(agencia, conta);

        if (!passwordEncoder.matches(senha, userDetails.getPassword())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }

        return new CustomAuthenticationToken(userDetails, senha, userDetails.getAuthorities(), agencia, conta);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
