package com.example.online_farm.Repository;

import com.example.online_farm.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
     Optional<User> findByEmail(String username);
}

