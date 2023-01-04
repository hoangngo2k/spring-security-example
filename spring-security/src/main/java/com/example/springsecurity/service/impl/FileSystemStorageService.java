package com.example.springsecurity.service.impl;

import com.example.springsecurity.entity.FileUpload;
import com.example.springsecurity.error.StorageException;
import com.example.springsecurity.error.StorageFileNotFoundException;
import com.example.springsecurity.repository.FileRepository;
import com.example.springsecurity.service.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;
    private final FileRepository fileRepository;

    public FileSystemStorageService(FileRepository fileRepository) {
        this.rootLocation = Paths.get("src/main/resources/files/");
        this.fileRepository = fileRepository;
    }

    @Override
    public void loadFileFromDatabase() {
        List<FileUpload> fileList = fileRepository.findAll();
        fileList.forEach(file -> {
            try {
//                    newFile.mkdirs();
                PrintWriter pw = new PrintWriter(this.rootLocation + file.getFilename());
                byte[] content = Base64.getDecoder().decode(file.getContent().getBytes());
                pw.write(new String(content));
                pw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void store(MultipartFile file) {
        try {
            if (file.isEmpty())
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            FileUpload saveFile = new FileUpload();
            saveFile.setFilename(file.getOriginalFilename());
            saveFile.setContent(Base64.getEncoder().encodeToString(file.getInputStream().readAllBytes()));
            fileRepository.save(saveFile);
            Files.copy(file.getInputStream(), this.rootLocation.resolve(Objects.requireNonNull(file.getOriginalFilename())));
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename());
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))
                    .map(path -> this.rootLocation.relativize(path));
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
    }

    @Override
    public Path load(String filename) {
        return this.rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path path = load(filename);
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

}
