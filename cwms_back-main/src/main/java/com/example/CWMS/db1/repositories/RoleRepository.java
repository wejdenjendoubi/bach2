package com.example.CWMS.db1.repositories;

import com.example.CWMS.db1.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Charge tous les rôles.
     * Les collections users et menuMappings sont LAZY — elles ne sont
     * jamais touchées dans toDTO() car on passe par les repositories
     * dédiés. findAll() standard suffit ici.
     */
    @Override
    List<Role> findAll();

    Optional<Role> findByRoleName(String roleName);
}