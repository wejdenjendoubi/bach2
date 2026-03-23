package com.example.CWMS.iservice;

import com.example.CWMS.dto.UserDTO;
import com.example.CWMS.db1.entities.User;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Integer id);
    UserDTO createUser(UserDTO userDTO);
    UserDTO updateUser(Integer id, UserDTO userDTO);
    void deleteUser(Integer id);
    void forceDeleteUser(Integer id);
    UserDTO mapToDTO(User user);
}
