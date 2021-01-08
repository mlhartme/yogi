package net.mlhartme.yogi;

import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
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
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableWebSecurity
public class YogiSecurity extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable() // TODO: post logout and get rid of this ...
                .authorizeRequests()
                .antMatchers("/", "/webjars/**").permitAll()
                .anyRequest().authenticated()
              .and()
                .formLogin()
                .loginPage("/")
                .successHandler(successHandler())
                .permitAll()
              .and()
                .logout()
                .logoutSuccessUrl("/")
                .permitAll();
    }

    private static SavedRequestAwareAuthenticationSuccessHandler successHandler() {
        SavedRequestAwareAuthenticationSuccessHandler result;

        result = new SavedRequestAwareAuthenticationSuccessHandler();
        result.setDefaultTargetUrl("/books");
        return result;
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        World world;
        FileNode src;
        Properties p;
        InMemoryUserDetailsManager result;

        world = World.createMinimal();
        src = world.file("/usr/local/yogi/etc/user.properties");
        try {
            p = src.exists() ? src.readProperties() : new Properties();
        } catch (IOException e) {
            throw new IllegalStateException("cannot load user.properties", e);
        }
        result = new InMemoryUserDetailsManager();
        for (Map.Entry<Object, Object> entry : p.entrySet()) {
            result.createUser(user((String) entry.getKey(), (String) entry.getValue()));
        }
        return result;
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
