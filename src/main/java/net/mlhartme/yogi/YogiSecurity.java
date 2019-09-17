package net.mlhartme.yogi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableWebSecurity
public class YogiSecurity extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().authenticated()
              .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
              .and()
                .logout()
                .permitAll();
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(user("Jakob", "3011"), user("Benjamin", "0908"));
    }

    private static UserDetails user(String name, String pw) {
        return User.withDefaultPasswordEncoder()
                .username(name)
                .password(pw)
                .roles("USER")
                .build();
    }

    //--

    public static String username() {
        User user;

        user = userOpt();
        return user == null ? "anonymous" : user.getUsername();
    }

    /** return null if not authenticated */
    public static User userOpt() {
        Authentication authentication;

        authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return (User) authentication.getPrincipal();
    }
}
