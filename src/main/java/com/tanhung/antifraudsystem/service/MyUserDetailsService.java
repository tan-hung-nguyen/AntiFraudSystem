package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.model.User;
import com.tanhung.antifraudsystem.model.UserPrinciple;
import com.tanhung.antifraudsystem.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {


    UserRepo userRepo;

    @Autowired
    public MyUserDetailsService(UserRepo userRepo){
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User userFound = userRepo.findByUsername(username.toLowerCase());

        if(userFound == null){
            throw new UsernameNotFoundException("User not found!");
        }
        return new UserPrinciple(userFound);
    }
}
