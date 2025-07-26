package com.project.shopapp.services;

import com.project.shopapp.components.JwtTonkenUtil;
import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.ChangePasswordRequest;
import com.project.shopapp.dtos.UpdateUserDTO;
import com.project.shopapp.dtos.UserDTO;
import com.project.shopapp.exception.DataNotFoundException;
import com.project.shopapp.exception.PermissionDenyException;
import com.project.shopapp.models.Role;
import com.project.shopapp.models.User;
import com.project.shopapp.repositories.RoleRepository;
import com.project.shopapp.repositories.UserRepository;
import com.project.shopapp.response.UpdateUserResponse;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTonkenUtil jwtTonkenUtil;
    private final AuthenticationManager authenticationManager;
    private final LocalizationUtils localizationUtils;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllUsersWithUserRole();
    }

    @Override
    @Transactional
    public User createUser(UserDTO userDTO) throws Exception {
        String phoneNumber = userDTO.getPhoneNumber();
        //kiểm tra sđt đã tồn tại hay chưa
        if (userRepository.existsByPhoneNumber(phoneNumber)){
            throw new DataIntegrityViolationException("Phone number already exits");
        }
        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(()-> new DataNotFoundException("Role not found"));
        if (role.getName().toUpperCase().equals(Role.ADMIN)){
            throw new PermissionDenyException("You cannot register an admin account!");
        }
        //convert userDTO => user
        User newUser = User.builder()
                .fullName(userDTO.getFullName())
                .email(userDTO.getEmail())
                .phoneNumber(userDTO.getPhoneNumber())
                .password(userDTO.getPassword())
                .address(userDTO.getAddress())
                .zipCode(userDTO.getZipCode())
                .prefecture(userDTO.getPrefecture())
                .city(userDTO.getCity())
                .addressLine1(userDTO.getAddressLine1())
                .addressLine2(userDTO.getAddressLine2())
                .dateOfBirth(userDTO.getDateOfBirth())
                .facebookAccountId(userDTO.getFacebookAccountId())
                .googleAccountId(userDTO.getGoogleAccountId())
                .build();
        newUser.setRole(role);

        //kiểm tra nếu có accountId, không thi yêu cầu pasword
        if ((userDTO.getFacebookAccountId() == null )
                && (userDTO.getGoogleAccountId() == null )) {
            String password = userDTO.getPassword();
            String encodePassword = passwordEncoder.encode(password);
            newUser.setPassword(encodePassword);

        }
        return userRepository.save(newUser);
    }

    @Override
    public String login(String phoneNumber, String password, Long roleId) throws Exception {

        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);
        if(optionalUser.isEmpty()){
            throw new DataNotFoundException("Invalid phoneNumber / password!");
        }

        User existingUser = optionalUser.get();

        // ✅ Nếu user login thường (không có Google/Facebook ID), thì mới kiểm tra mật khẩu
        boolean isGoogle = existingUser.getGoogleAccountId() != null ;
        boolean isFacebook = existingUser.getFacebookAccountId() != null ;

        //check password
        if (!isGoogle || !isFacebook){
            if (!passwordEncoder.matches(password,existingUser.getPassword())){
                throw  new BadCredentialsException("Wrong phone number or password !");
            }
        }

        Optional<Role> optionalRole = roleRepository.findById(roleId);
        if (optionalRole.isEmpty() || !roleId.equals(existingUser.getRole().getId())){
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.ROLE_DOES_NOT_EXISTS));
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
          phoneNumber,password,
                existingUser.getAuthorities()
        );
        //authenticate with java Spring security
        authenticationManager.authenticate(authenticationToken);
        return jwtTonkenUtil.generateToken(existingUser);
    }



    @Override
    public User getUserDetailsFromToken(String token) throws Exception{
        if(jwtTonkenUtil.isTokenExpired(token)){
            throw  new Exception("Token is expired");
        }
        /*String phoneNumber = jwtTonkenUtil.extractPhoneNumber(token);
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);

        if (user.isPresent()){
            return user.get();
        }else {
            throw new Exception("User not found");
        }*/
        Long userId = jwtTonkenUtil.extractUserId(token);
        Optional<User> user = userRepository.findById(userId);

        if (user.isPresent()) {
            return user.get();
        } else {
            throw new Exception("User not found");
        }
    }

    @Override
    public String updateProfile(Long userId, UpdateUserDTO updateUserDTO) throws Exception {
        User user = userRepository.findById(userId).orElseThrow(
                ()-> new DataNotFoundException("user not found"));
        boolean phoneChange = false;
        boolean emailChange = false;

        if (user.getPhoneNumber() == null) {
            phoneChange = true;
        }else {
            phoneChange = !user.getPhoneNumber().equals(updateUserDTO.getPhoneNumber());
        }

        if (user.getEmail() == null) {
            emailChange = true;
        }else {
            emailChange = !user.getEmail().equals(updateUserDTO.getEmail());
        }


        user.setAddress(updateUserDTO.getAddress());
        user.setZipCode(updateUserDTO.getZipCode());
        user.setPrefecture(updateUserDTO.getPrefecture());
        user.setCity(updateUserDTO.getCity());
        user.setAddressLine1(updateUserDTO.getAddressLine1());
        user.setAddressLine2(updateUserDTO.getAddressLine2());
        if (emailChange){
            Optional<User> existingUserByEmail = userRepository.findByEmail(updateUserDTO.getEmail());
            if (existingUserByEmail.isPresent() && !existingUserByEmail.get().getId().equals(userId)){
                throw  new IllegalArgumentException("email này đã được sử dụng");
            }

            user.setEmail(updateUserDTO.getEmail());
        }


        if(phoneChange){

            Optional<User> existingUser = userRepository.findByPhoneNumber(updateUserDTO.getPhoneNumber());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
                throw new IllegalArgumentException("Số điện thoại này đã được sử dụng bởi người dùng khác.");
            }

            user.setPhoneNumber(updateUserDTO.getPhoneNumber());
            userRepository.save(user);
            return jwtTonkenUtil.generateToken(user);
        }else {
            userRepository.save(user);
            return null;
        }





    }

    @Override
    @Transactional
    public void changePassword(String phoneNumber, ChangePasswordRequest request) throws DataNotFoundException {

        User user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow(
                ()-> new DataNotFoundException("User not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())){
            throw new RuntimeException("Current password is incorrect!");

        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        }

    @Override
    @Transactional
    public UpdateUserResponse getProfile(Long userId) throws DataNotFoundException {
        User user = userRepository.findById(userId).orElseThrow(
                ()-> new DataNotFoundException("User not found"));
        return UpdateUserResponse.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .address(user.getAddress())
                .zipCode(user.getZipCode())
                .prefecture((user.getPrefecture()))
                .city(user.getCity())
                .addressLine1(user.getAddressLine1())
                .addressLine2(user.getAddressLine2())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .build();
    }


}
