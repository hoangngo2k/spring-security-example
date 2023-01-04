package com.example.springsecurity.service;

import com.example.springsecurity.entity.Role;
import com.example.springsecurity.enums.ERole;
import com.example.springsecurity.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoleService {

    private final RoleRepository repository;

    public RoleService(RoleRepository repository) {
        this.repository = repository;
    }

    public Optional<Role> findByName(ERole role) {
        return repository.findByName(role);
    }
}
