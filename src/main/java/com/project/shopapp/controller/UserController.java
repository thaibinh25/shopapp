package com.project.shopapp.controller;

import com.project.shopapp.dtos.ChangePasswordRequest;
import com.project.shopapp.dtos.UpdateUserDTO;
import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.dtos.UserLoginDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.models.User;
import com.project.shopapp.response.*;
import com.project.shopapp.services.UserService;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final LocalizationUtils localizationUtils;

    @GetMapping("")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userService.getAllUsers(); // hoặc to DTO
        return ResponseEntity.ok(users);
    }



    @GetMapping("/profile")
    public ResponseEntity<UpdateUserResponse> getProfile(Principal principal) throws DataNotFoundException {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.getProfile(currentUser.getId()));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> createUser(
            @Valid @RequestBody UserDTO userDTO,
            BindingResult result){
        RegisterResponse registerResponse= new RegisterResponse();
        try{
            if (result.hasErrors()){
                List<String> errorMessages = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(
                        RegisterResponse.builder()
                                .message(localizationUtils.getLocalizedMessage(MessageKeys.REGISTER_SUCCESSFULLY))
                                .build()
                );
            }
            if(!userDTO.getPassword().equals(userDTO.getRetypePassword())){
                return ResponseEntity.badRequest().body(
                        RegisterResponse.builder()
                                .message(localizationUtils.getLocalizedMessage(MessageKeys.PASSWORD_NOT_MATCH))
                                .build());
            }
            User user = userService.createUser(userDTO);

            return ResponseEntity.ok(RegisterResponse.builder().user(user).build());
        }catch(Exception ex){
            registerResponse.setMessage(ex.getMessage());
            return ResponseEntity.badRequest().body(registerResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody UserLoginDTO userLoginDTO
    ) throws Exception {
        //Kiểm tra thông tin đăng nhập và sinh token
        try {
              String token = userService.login(
                    userLoginDTO.getPhoneNumber(),
                    userLoginDTO.getPassword(),
                    userLoginDTO.getRoleId() == null ? 1 : userLoginDTO.getRoleId());
            //Trả về token trong  response
            return ResponseEntity.ok(LoginResponse.builder()
                    .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_SUCCESSFULLY))
                    .token(token)
                    .build());
        }catch (Exception e){
            return   ResponseEntity.badRequest().body(
                    LoginResponse.builder()
                         .message(localizationUtils.getLocalizedMessage(MessageKeys.LOGIN_FAILED,e.getMessage()))
                            .build()
            );
        }
    }

    @PostMapping("/details")
    public ResponseEntity<?> getUserDetails(@RequestHeader("Authorization") String authorizationHeader){
        try{
            String extractedToken = authorizationHeader.substring(7);//loại bỏ bearer
            User user = userService.getUserDetailsFromToken(extractedToken);
            return ResponseEntity.ok(UserResponse.fromUser(user));

        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateUserDTO updateUserDTO){
       try{
           User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
           String newToken = userService.updateProfile(currentUser.getId(), updateUserDTO);

           return ResponseEntity.ok(UpdateUserProfileResponse.builder()
                            .message(localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_USER_PROFILE_SUCCESSFULLY))
                            .newToken(newToken)
                            .build());

       }catch (Exception e){
           return ResponseEntity.badRequest().body(e.getMessage());
       }
    }


    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, Principal principal)  {
        try {
            userService.changePassword(principal.getName(), request);
            return ResponseEntity.ok(Map.of("message", localizationUtils.getLocalizedMessage(MessageKeys.CHANGE_PASSWORD_SUCCESSFULLY)));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));
        }
    }



}
