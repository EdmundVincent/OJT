package com.example.staffassign.service;

import com.example.staffassign.entity.UserMaster;
import com.example.staffassign.repository.UserMasterMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.staffassign.dto.UserCsvDto;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.CSVWriter;
import org.springframework.transaction.annotation.Transactional;
import java.io.Reader;
import java.io.Writer;
import java.util.stream.Collectors;
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

    @Transactional
    public void importCsv(Reader reader) {
        List<UserCsvDto> dtos = new CsvToBeanBuilder<UserCsvDto>(reader)
                .withType(UserCsvDto.class)
                .build()
                .parse();

        for (UserCsvDto dto : dtos) {
            UserMaster user = new UserMaster();
            user.setEmail(dto.getEmail());
            user.setRole(dto.getRole());
            user.setValidFrom(dto.getValidFrom());
            user.setValidTo(dto.getValidTo());
            
            if (userMasterMapper.findByEmail(dto.getEmail()).isPresent()) {
                update(user, dto.getPassword());
            } else {
                create(user, dto.getPassword() != null ? dto.getPassword() : "password");
            }
        }
    }

    public void exportCsv(Writer writer) throws Exception {
        List<UserMaster> users = userMasterMapper.findAll();
        List<UserCsvDto> dtos = users.stream().map(u -> {
            UserCsvDto dto = new UserCsvDto();
            dto.setEmail(u.getEmail());
            dto.setRole(u.getRole());
            dto.setValidFrom(u.getValidFrom());
            dto.setValidTo(u.getValidTo());
            return dto;
        }).collect(Collectors.toList());

        new StatefulBeanToCsvBuilder<UserCsvDto>(writer)
                .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                .build()
                .write(dtos);
    }
}
