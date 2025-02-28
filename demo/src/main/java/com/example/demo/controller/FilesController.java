package com.example.demo.controller;

import com.example.demo.service.FilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FilesController {
    @Autowired
    private FilesService filesService;

    @GetMapping
    public ResponseEntity<byte[]> getLastFile(){
        byte[] imageData = filesService.getAllFiles();
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.valueOf("image/png")).body(imageData);
    }

    @PostMapping("/uploadFilesIntoDB")
    public ResponseEntity<String> storeFilesIntoDB(@RequestParam("file") MultipartFile file) throws IOException {
        String response = filesService.storeFile(file);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/uploadFilesIntoDB/edited")
    public ResponseEntity<String> storeEditedFilesIntoDB(
            @RequestParam("file") MultipartFile file,
            @RequestParam("action") List<String> action,
            @RequestParam(value = "brightness", required = false, defaultValue = "50") int brightness,
            @RequestParam(value = "salt_prob", required = false, defaultValue = "0.01") float salt_prob,
            @RequestParam(value = "pepper_prob", required = false, defaultValue = "0.01") float pepper_prob) throws IOException {
        String response = filesService.storeEditedFile(file, action, brightness, salt_prob, pepper_prob);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/getFileByName/{fileName}")
    public ResponseEntity<byte[]> getFile(@PathVariable String fileName) {
        byte[] imageData = filesService.getFiles(fileName);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.valueOf("image/png")).body(imageData);
    }
}