package com.minibank.authservice.Services;

import com.minibank.authservice.Entity.Role;
import com.minibank.authservice.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Transactional
    public void initializeRoles() {
        createRoleIfNotExists("ADMIN", "Full system access, user management, transaction oversight");
        createRoleIfNotExists("CUSTOMER", "Account access, transactions, profile management");
        createRoleIfNotExists("MANAGER", "Customer support, limited admin functions");
        createRoleIfNotExists("AUDITOR", "Read-only access to transactions and reports");
    }

    private void createRoleIfNotExists(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role(name, description);
            roleRepository.save(role);
        }
    }
}
