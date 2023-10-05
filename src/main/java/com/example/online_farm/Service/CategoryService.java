package com.example.online_farm.Service;

import com.example.online_farm.Entity.Category;
import com.example.online_farm.Entity.User;
import com.example.online_farm.Repository.CategoryRepository;
import com.example.online_farm.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public String addUser(User userInfo) {
        userInfo.setPassWord(passwordEncoder.encode(userInfo.getPassWord()));
        repository.save(userInfo);
        return "user added to system ";
    }
}
