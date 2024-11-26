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


    /**
     * Filtra as requisições HTTP para autenticar usuários com base em tokens JWT
     * @param request A requisição HTTP a ser filtrada
     * @param response A resposta HTTP
     * @param filterChain A cadeia de filtros para continuar o processamento
     * @throws ServletException Se ocorrer um erro durante o processamento do servlet
     * @throws IOException Se ocorrer um erro de I/O
     */
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


    /**
     * Extrai o token JWT do cabeçalho Authorization da requisição HTTP
     * @param request A requisição HTTP da qual extrair o token
     * @return O token JWT sem o prefixo "Bearer" ou null se não houver token
     */
    private String headerToken(HttpServletRequest request){
        String authorizationHeader = request.getHeader("Authorization");
        
        if (authorizationHeader != null) {
          return authorizationHeader.replace("Bearer ", "");
        }

        return null;
    }
}
