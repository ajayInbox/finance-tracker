package com.finance.tracker.transactions.service;

import java.io.InputStream;

public interface FileReaderService {
    void readPdf(InputStream inputStream);
}
