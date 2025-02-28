package com.example.demo.doe;

import com.example.demo.entity.Files;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<Files, Long> {
    Files findByName(String name);
}
