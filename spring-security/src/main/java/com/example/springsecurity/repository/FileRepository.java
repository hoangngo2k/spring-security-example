package com.example.springsecurity.repository;

import com.example.springsecurity.entity.FileUpload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileUpload, Long> {

    FileUpload findByFilename(String filename);
}
