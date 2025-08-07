package com.project.shopapp.services;

import com.project.shopapp.dtos.ChangePasswordRequest;
import com.project.shopapp.dtos.UpdateUserDTO;
import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.User;
import com.project.shopapp.response.UpdateUserResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IUserService {
    List<User> getAllUsers();
    User createUser (UserDTO userDTO) throws Exception;

    //User updateUser(Long userId, UpdateUserDTO updateUserDTO) throws Exception;

    User login(String phoneNumber, String password) throws Exception;

    User getUserDetailsFromToken(String token) throws Exception;

    String updateProfile(Long userId, UpdateUserDTO updateUserDTO) throws Exception;
    void changePassword(String phoneNumber, ChangePasswordRequest changePasswordRequest) throws DataNotFoundException;
    UpdateUserResponse getProfile(Long userId) throws DataNotFoundException;
}
