package br.com.bb.banco.security;

import java.io.IOException;
import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.auth0.jwt.interfaces.Claim;
import br.com.bb.banco.entity.ClienteConta;
import br.com.bb.banco.repository.ClienteContaRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    JwtUtils jwtUtils;

    ClienteContaRepository clienteContaRepository;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, ClienteContaRepository clienteContaRepository) {
        this.jwtUtils = jwtUtils;
        this.clienteContaRepository = clienteContaRepository;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = headerToken(request);

        if(token != null){
            Map<String, Claim> claims = jwtUtils.validarTokenJwt(token);

            String agencia = claims.get("agencia").asString();
            String conta = claims.get("conta").asString();

            ClienteConta clienteConta = clienteContaRepository.findByAgenciaAndNumeroDaConta(agencia, conta).get();
            
            UserDetailsImpl userDetails = new UserDetailsImpl(clienteConta);
            
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }


    String headerToken(HttpServletRequest request){
        
        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader != null) {
          return authorizationHeader.replace("Bearer ", "");
        }

        return null;
    }

    
}
