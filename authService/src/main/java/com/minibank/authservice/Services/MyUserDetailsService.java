package com.minibank.authservice.Services;

import com.minibank.authservice.Entity.UserPrincipal;
import com.minibank.authservice.Entity.Users;
import com.minibank.authservice.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Users users = userRepository.findByUsername(username);

        if (users == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new UserPrincipal(users);
    }
}
