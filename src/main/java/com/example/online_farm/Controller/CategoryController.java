package com.example.online_farm.Controller;

import com.example.online_farm.Config.UserInfoUserDetails;
import com.example.online_farm.DTO.*;
import com.example.online_farm.Entity.Category;
import com.example.online_farm.Entity.RefreshToken;
import com.example.online_farm.Entity.Role;
import com.example.online_farm.Entity.User;
import com.example.online_farm.Repository.UserRepository;
import com.example.online_farm.Service.CategoryService;
import com.example.online_farm.Service.JwtService;
import com.example.online_farm.Service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class CategoryController {
    @Autowired
    private CategoryService service;
    @Autowired
    private UserRepository repository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/new")
    public ResponseEntity<AuthRegister> addUser(@RequestBody AuthRequest user) {
        User newUser = service.addUser(user);

        // Tạo một đối tượng UserDetails
        UserDetails userDetails = new UserInfoUserDetails(newUser);

        // Tạo token cho người dùng mới đăng ký
        String token = jwtService.generateToken(newUser.getEmail());

        // Xây dựng AuthRegister
        AuthRegister authResponse = new AuthRegister();
        authResponse.setMessage("Đăng kí thành công");

        UserRegister userData = new UserRegister();
        userData.setAccessToken("Bearer " + token);

        // Lấy thời gian hiện tại và chuyển đổi sang java.util.Date
        LocalDateTime currentTime = LocalDateTime.now();
        Date createdAt = Date.from(currentTime.atZone(ZoneId.systemDefault()).toInstant());
        userData.setCreatedAt(createdAt);

        List<String> roleNames = newUser.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());
        userData.setRoles(roleNames);

        authResponse.setUserRegister(userData);

        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }





    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }
    @GetMapping("/categories")
    public CategoryResponse getAll(){
        List<Category> categories = service.getAll();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> {
                    CategoryDTO categoryDTO = new CategoryDTO();
                    categoryDTO.set_id(String.valueOf(category.getId()));
                    categoryDTO.setName(category.getName())
                    return categoryDTO;
                })
                .collect(Collectors.toList());

        CategoryResponse response = new CategoryResponse();
        response.setMessage("Lấy categories thành công");
        response.setData(categoryDTOs);

        return response;
    }



    @PostMapping("/authenticate")
    public AuthResponse authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(authRequest.getUsername());
            Object principal = authentication.getPrincipal();
            UserDetails userDetails = (UserDetails) principal;
            Optional<User> user = repository.findByEmail(userDetails.getUsername());

            UserData userData = new UserData();
            userData.setAccessToken("Bearer " + token);
            userData.setName(user.get().getFullName());
            userData.setPhone(user.get().getSdt());
            userData.getCreatedAt();
            userData.getAddress();
            userData.getPhone();
            userData.getUpdatedAt();
            List<String> roleNames = user.get().getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            userData.setRoles(roleNames);

            // Tạo refreshToken
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequest.getUsername());

            // Tạo và trả về AuthResponse chứa cả refreshToken
            AuthResponse authResponse = new AuthResponse();
            authResponse.setMessage("Đăng nhập thành công");
            authResponse.setData(userData);
            authResponse.setRefreshToken(refreshToken.getToken());

            return authResponse;
        } else {
            throw new UsernameNotFoundException("invalid user request !");
        }
    }

    @PostMapping("/refreshToken")
    public JwtResponse refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return refreshTokenService.findByToken(refreshTokenRequest.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(userInfo -> {
                    String accessToken = jwtService.generateToken(userInfo.getFullName());
                    return JwtResponse.builder()
                            .accessToken(accessToken)
                            .token(refreshTokenRequest.getToken())
                            .build();
                }).orElseThrow(() -> new RuntimeException(
                        "Refresh token is not in database!"));
    }
}
