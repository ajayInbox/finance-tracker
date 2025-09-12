package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.transactions.service.FileReaderService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FileReaderServiceImpl implements FileReaderService {
    @Override
    public void readPdf(InputStream inputStream) {
        System.out.println("hi");
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            System.out.println(pdfStripper.getText(document));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
