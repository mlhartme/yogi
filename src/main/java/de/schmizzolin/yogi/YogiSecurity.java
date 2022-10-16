/*
 * Copyright Michael Hartmeier, https://github.com/mlhartme/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.schmizzolin.yogi;

import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import org.springframework.beans.factory.annotation.Value;
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
    private final FileNode userProperties;

    public YogiSecurity(World world, @Value("${yogi.user}") String etc) {
        userProperties = world.file(etc);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/", "/static/**", "/webjars/**").permitAll()
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
        Properties p;
        InMemoryUserDetailsManager result;

        try {
            p = userProperties.readProperties();
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
