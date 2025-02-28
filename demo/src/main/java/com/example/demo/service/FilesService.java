package com.example.demo.service;

import com.example.demo.doe.FileRepository;
import com.example.demo.entity.Files;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FilesService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${python.api.url}")
    private String pythonApiUrl;

    @Autowired
    private FileRepository fileRepository;

    public String storeFile( MultipartFile file) throws IOException {
        Files files = Files
                .builder()
                .name(file.getOriginalFilename())
                .type(file.getContentType())
                .imageData(file.getBytes())
                .build();
        files = fileRepository.save(files);

        if(files.getId() != null) {
            return "Added successfully";
        }
        return null;
    }


//public String storeEditedFile(MultipartFile file, List<String> action, int brightness, float salt_prob, float pepper_prob) throws IOException {
//    byte[] zipFile = processImage(file.getBytes(), action, brightness, salt_prob, pepper_prob);
//    try (ByteArrayInputStream bais = new ByteArrayInputStream(zipFile);
//         ZipInputStream zis = new ZipInputStream(bais)) {
//        ZipEntry entry;
//        while ((entry = zis.getNextEntry()) != null) {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            byte[] buffer = new byte[1024];
//            int len;
//            while ((len = zis.read(buffer)) > 0) {
//                baos.write(buffer, 0, len);
//            }
//
//            byte[] processedImage = baos.toByteArray();
//            Files files = Files.builder()
//                    .name(entry.getName())
//                    .type("image/png")
//                    .imageData(processedImage)
//                    .build();
//            fileRepository.save(files);
//        }
//    }
//
//    return "Added successfully";
//}
public String storeEditedFile(MultipartFile file, List<String> action, int brightness, float salt_prob, float pepper_prob) throws IOException {
    // Pass the original MultipartFile instead of file.getBytes()
    byte[] zipFile = processImage(file, action, brightness, salt_prob, pepper_prob);

    try (ByteArrayInputStream bais = new ByteArrayInputStream(zipFile);
         ZipInputStream zis = new ZipInputStream(bais)) {
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }

            byte[] processedImage = baos.toByteArray();
            Files files = Files.builder()
                    .name(entry.getName())  // Use the name from the ZipEntry
                    .type("image/png")
                    .imageData(processedImage)
                    .build();
            fileRepository.save(files);  // Save each processed image to the database
        }
    }

    return "Files added successfully";
}

//private byte[] processImage(byte[] imageData, List<String> actions, int brightness, float salt_prob, float pepper_prob) throws IOException {
//    HttpHeaders headers = new HttpHeaders();
//    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//    body.add("file", new ByteArrayResource(imageData) {
//        @Override
//        public String getFilename() {
//            return "image.png";
//        }
//    });
//
//    StringBuilder actionParams = new StringBuilder();
//    boolean firstAction = true;
//
//    for (String action : actions) {
//        if (firstAction) {
//            actionParams.append("?action=").append(action);
//            firstAction = false;
//        } else {
//            actionParams.append("&action=").append(action);
//        }
//
//        switch (action) {
//            case "brightness" -> actionParams.append("&brightness=").append(brightness);
//            case "noise" -> actionParams.append("&salt_prob=").append(salt_prob).append("&pepper_prob=").append(pepper_prob);
//        }
//    }
//
//    String url = pythonApiUrl + "/process-image" + actionParams;
//
//    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//
//    ResponseEntity<byte[]> response = restTemplate.exchange(
//            url,
//            HttpMethod.POST,
//            requestEntity,
//            byte[].class
//    );
//
//    return response.getBody();
//}
private byte[] processImage(MultipartFile file, List<String> actions, int brightness, float salt_prob, float pepper_prob) throws IOException {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    // Extract original file name from MultipartFile
    String originalFilename = file.getOriginalFilename();

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ByteArrayResource(file.getBytes()) {
        @Override
        public String getFilename() {
            // Use the original filename instead of "image.png"
            return originalFilename != null ? originalFilename : "image.png";
        }
    });

    StringBuilder actionParams = new StringBuilder();
    boolean firstAction = true;

    for (String action : actions) {
        if (firstAction) {
            actionParams.append("?action=").append(action);
            firstAction = false;
        } else {
            actionParams.append("&action=").append(action);
        }

        switch (action) {
            case "brightness" -> actionParams.append("&brightness=").append(brightness);
            case "noise" -> actionParams.append("&salt_prob=").append(salt_prob).append("&pepper_prob=").append(pepper_prob);
        }
    }

    String url = pythonApiUrl + "/process-image" + actionParams;

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    ResponseEntity<byte[]> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            requestEntity,
            byte[].class
    );

    return response.getBody();
}


    public byte[] getAllFiles(){
        return fileRepository.findAll().getLast().getImageData();
    }

    public byte[] getFiles(String fileName){
        return fileRepository.findByName(fileName).getImageData();
    }
}
