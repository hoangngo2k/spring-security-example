package com.example.springsecurity.mapper;

import com.example.springsecurity.entity.User;
import com.example.springsecurity.repository.UserRepository;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;

@Component
public class CSVFileMapper {

    private final UserRepository userRepository;

    public CSVFileMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<String[]> createCsvData() {
        List<User> list = userRepository.findAll();
        List<String[]> dataLines = new ArrayList<>();
        list.forEach(user -> {
            String[] data = {user.getId() + "", user.getUsername(), user.getEmail(), user.getPassword()};
            dataLines.add(data);
        });
        return dataLines;
    }

    public void saveCsvFile(String filename) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
            writer.writeAll(createCsvData());
//            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
