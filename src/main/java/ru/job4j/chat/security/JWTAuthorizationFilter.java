package ru.job4j.chat.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;
import ru.job4j.chat.domain.Person;
import ru.job4j.chat.domain.Role;
import ru.job4j.chat.repository.PersonRepository;
import ru.job4j.chat.repository.RoleRepository;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static ru.job4j.chat.security.JWTAuthenticationFilter.HEADER_STRING;
import static ru.job4j.chat.security.JWTAuthenticationFilter.SECRET;
import static ru.job4j.chat.security.JWTAuthenticationFilter.TOKEN_PREFIX;

@Component
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {
    private PersonRepository personRepository;
    private RoleRepository roleRepository;

    public JWTAuthorizationFilter(AuthenticationManager authManager,
                                  PersonRepository personRepository,
                                  RoleRepository roleRepository) {
        super(authManager);
        this.personRepository = personRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        String header = req.getHeader(HEADER_STRING);

        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            chain.doFilter(req, res);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);
        if (token != null) {
            String user = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
                    .build()
                    .verify(token.replace(TOKEN_PREFIX, ""))
                    .getSubject();

            if (user != null) {
                Person person = personRepository.findByName(user).get();
                Role role = roleRepository.findById(person.getRoleId()).get();
                return new UsernamePasswordAuthenticationToken(user, null, List.of(role));
            }
            return null;
        }
        return null;
    }
}