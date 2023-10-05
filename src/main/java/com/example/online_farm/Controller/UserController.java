package com.example.online_farm.Controller;

import com.example.online_farm.DTO.UserAllDTO;
import com.example.online_farm.DTO.UserAllMessiage;
import com.example.online_farm.DTO.UserUpdateRequest;
import com.example.online_farm.Entity.Role;
import com.example.online_farm.Entity.User;
import com.example.online_farm.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserAllMessiage> getAllUsers() {
        List<User> users = userRepository.findAll();
        // Chuyển đổi từ User sang UserDTO và loại bỏ trường password
        List<UserAllDTO> userDTOs = users.stream()
                .map(user -> {
                    UserAllDTO userDTO = new UserAllDTO();
                    userDTO.setId(user.getId());
                    userDTO.setEmail(user.getEmail());
                    userDTO.setName(user.getFullName());
                    userDTO.setAddress(user.getAddress());
                    userDTO.setPhone(user.getSdt());
                    userDTO.setCreatedAt(null);
                    userDTO.setUpdateAt(null);
                    List<String> roleNames = user.getRoles().stream()
                            .map(Role::getName) // giả sử Role có phương thức getName() để lấy tên của vai trò
                            .collect(Collectors.toList());
                    userDTO.setRoleList(roleNames);
                    // Thêm các trường khác mà bạn muốn trả về
                    return userDTO;
                })
                .collect(Collectors.toList());

        UserAllMessiage allMessiage = new UserAllMessiage();
        allMessiage.setMasseage("Lấy nguoi dung thanh cong");
        allMessiage.setUserAllDTOS(userDTOs);
        return new ResponseEntity<>(allMessiage, HttpStatus.OK);
    }
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable int id, @RequestBody UserUpdateRequest userUpdateRequest) {
        User existingUser = userRepository.findById(id).orElse(null);

        if (existingUser == null) {
            // Trả về lỗi 404 Not Found
            return ResponseEntity.notFound().build();
        }

        // Update the user's information based on the request body
        existingUser.setAddress(userUpdateRequest.getAddress());
        existingUser.setFullName(userUpdateRequest.getName());
        existingUser.setSdt(userUpdateRequest.getPhone());
        existingUser.setAvatar(userUpdateRequest.getAvatar());

        // Check if a new password is provided and update it if needed
        if (userUpdateRequest.getNewPassword() != null) {
            if (userUpdateRequest.getNewPassword().equals(userUpdateRequest.getPassword())) {
                existingUser.setPassWord(userUpdateRequest.getNewPassword());
            } else {
                // Handle password mismatch error
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        }

        // Save the updated user to the database
        User updatedUser = userRepository.save(existingUser);

        // Trả về phản hồi 200 OK với thông tin người dùng đã được cập nhật
        return ResponseEntity.ok(updatedUser);
    }

}
