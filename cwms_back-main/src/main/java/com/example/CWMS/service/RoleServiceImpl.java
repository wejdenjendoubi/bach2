package com.example.CWMS.service;

import com.example.CWMS.dto.RoleDTO;
import com.example.CWMS.dto.RoleMenuRequest;
import com.example.CWMS.iservice.RoleService;
import com.example.CWMS.model.MenuItem;
import com.example.CWMS.model.Role;
import com.example.CWMS.model.RoleMenuMapping;
import com.example.CWMS.repository.MenuItemRepository;
import com.example.CWMS.repository.RoleMenuMappingRepository;
import com.example.CWMS.repository.RoleRepository;
import com.example.CWMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository            roleRepository;
    private final RoleMenuMappingRepository roleMenuMappingRepository;
    private final MenuItemRepository        menuItemRepository;
    private final UserRepository            userRepository;

    // ── READ ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        if (roles.isEmpty()) return Collections.emptyList();

        Map<Integer, List<Integer>> menusByRole = loadMenusByRole();
        Map<Integer, Integer>       usersByRole = loadUserCountByRole();

        return roles.stream()
                .map(r -> toDTOWithMaps(r, menusByRole, usersByRole))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDTO getRoleById(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found: " + id));
        return toDTO(role);
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO request) {
        if (request.getRoleName() == null || request.getRoleName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom du rôle est obligatoire");
        }

        if (roleRepository.findByRoleName(request.getRoleName().trim()).isPresent()) {
            throw new IllegalArgumentException("Un rôle avec ce nom existe déjà");
        }

        Role role = Role.builder()
                .roleName(request.getRoleName().trim())
                .description(request.getDescription())
                .build();

        Role saved = roleRepository.save(role);
        return toDTO(saved);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public RoleDTO updateRole(Integer id, RoleDTO request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found: " + id));

        if (request.getRoleName() != null && !request.getRoleName().trim().isEmpty()) {
            String newName = request.getRoleName().trim();
            if (!newName.equals(role.getRoleName()) &&
                    roleRepository.findByRoleName(newName).isPresent()) {
                throw new IllegalArgumentException("Un rôle avec ce nom existe déjà");
            }
            role.setRoleName(newName);
        }

        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        return toDTO(roleRepository.save(role));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void deleteRole(Integer id) {
        roleMenuMappingRepository.deleteByRoleId(id);
        roleRepository.deleteById(id);
    }

    // ── ASSIGN MENUS ──────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void assignMenusToRole(RoleMenuRequest request) {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleId()));

        roleMenuMappingRepository.deleteByRoleId(request.getRoleId());

        if (request.getMenuItemIds() == null || request.getMenuItemIds().isEmpty()) return;

        List<MenuItem> menuItems = menuItemRepository.findAllById(request.getMenuItemIds());

        List<RoleMenuMapping> mappings = menuItems.stream()
                .map(menuItem -> RoleMenuMapping.builder()
                        .role(role)
                        .menuItem(menuItem)
                        .build())
                .collect(Collectors.toList());

        roleMenuMappingRepository.saveAll(mappings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getMenuIdsByRole(Integer roleId) {
        return roleMenuMappingRepository.findMenuItemIdsByRoleId(roleId);
    }

    // ── DTO helpers ───────────────────────────────────────────────────────────

    @Override
    public RoleDTO toDTO(Role role) {
        List<Integer> menuIds = roleMenuMappingRepository.findMenuItemIdsByRoleId(role.getRoleId());
        int userCount = userRepository.findByRoleId(role.getRoleId()).size();

        return buildRoleDTO(role, menuIds, userCount);
    }

    // Méthode privée → pas besoin de @Override ni de public
    private RoleDTO toDTOWithMaps(Role role,
                                  Map<Integer, List<Integer>> menusByRole,
                                  Map<Integer, Integer> usersByRole) {
        List<Integer> menuIds = menusByRole.getOrDefault(role.getRoleId(), Collections.emptyList());
        int userCount = usersByRole.getOrDefault(role.getRoleId(), 0);

        return buildRoleDTO(role, menuIds, userCount);
    }

    private RoleDTO buildRoleDTO(Role role, List<Integer> menuIds, int userCount) {
        return RoleDTO.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .menuItemIds(menuIds)
                .userCount(userCount)
                .build();
    }

    private Map<Integer, List<Integer>> loadMenusByRole() {
        return roleMenuMappingRepository.findAllRoleMenuMappings()
                .stream()
                .collect(Collectors.groupingBy(
                        row -> ((Number) row[0]).intValue(),
                        Collectors.mapping(
                                row -> ((Number) row[1]).intValue(),
                                Collectors.toList())
                ));
    }

    private Map<Integer, Integer> loadUserCountByRole() {
        return roleMenuMappingRepository.countUsersByRole()
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).intValue(),
                        row -> ((Number) row[1]).intValue()
                ));
    }
}