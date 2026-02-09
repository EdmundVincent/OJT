package com.example.staffassign.service;

import com.example.staffassign.entity.UserMaster;
import com.example.staffassign.repository.UserMasterMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserMasterMapper userMasterMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMasterMapper userMasterMapper, PasswordEncoder passwordEncoder) {
        this.userMasterMapper = userMasterMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserMaster> findAll() {
        return userMasterMapper.findAll();
    }

    public Optional<UserMaster> findByEmail(String email) {
        return userMasterMapper.findByEmail(email);
    }

    public void create(UserMaster user, String rawPassword) {
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userMasterMapper.insert(user);
    }

    public void update(UserMaster user, String rawPassword) {
        if (rawPassword != null && !rawPassword.isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
        }
        userMasterMapper.update(user);
    }

    public void delete(String email) {
        userMasterMapper.delete(email);
    }
}
