package io.github.orionhealth.xbdd.config;

import javax.ws.rs.HttpMethod;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class ServerSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http
			.authorizeRequests(a -> a
				.antMatchers("/", "/login", "/error", "/rest/user/loggedin", "/rest/attachment/**").permitAll()
				.antMatchers(HttpMethod.PUT, "/rest/reports/**").permitAll()
				.antMatchers(HttpMethod.POST, "/rest/reports/**").permitAll()
				.anyRequest().authenticated()
			)
			.csrf(c -> c.disable()) // TODO - we probably want CSRF on but it's failing PUT/POST atm for some reason.
			.logout(l -> l
				// No logout URL as the frontend provides the logout redirect.
				.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))
				.permitAll()
			);
		http.oauth2Login();
		http.formLogin();
	}

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
		auth
			.ldapAuthentication()
				.userDnPatterns("uid={0},ou=people")
				.groupSearchBase("ou=groups")
				.contextSource()
					.url("ldap://localhost:8389/dc=springframework,dc=org")
					.and()
				.passwordCompare()
					.passwordEncoder(new BCryptPasswordEncoder())
					.passwordAttribute("userPassword");
	}

}
