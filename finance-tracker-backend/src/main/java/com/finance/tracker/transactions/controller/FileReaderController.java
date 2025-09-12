package com.finance.tracker.transactions.controller;

import com.finance.tracker.transactions.service.FileReaderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FileReaderController {

    private final FileReaderService readerService;

    @PostMapping("/pdfFile")
    ResponseEntity<Void> uploadPdf(@RequestParam("file")MultipartFile pdfFile){
        try {
            readerService.readPdf(pdfFile.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.noContent().build();
    }
}
