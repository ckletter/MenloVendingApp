package com.example.menlovending.stripe.manager;

import android.util.Log;

import com.stripe.stripeterminal.external.models.Reader;
import java.util.ArrayList;
import java.util.List;

public class ReaderManager {
    private static ReaderManager instance;
    private List<Reader> readers;

    private ReaderManager() {
        readers = new ArrayList<>();
    }

    public static ReaderManager getInstance() {
        if (instance == null) {
            instance = new ReaderManager();
        }
        return instance;
    }

    public void setReaders(List<Reader> readers) {
        this.readers = readers;
    }

    public List<Reader> getReaders() {
        return readers;
    }

    public Reader getReaderBySerial(String serialNumber) {
        for (Reader reader : readers) {
            if (reader.getSerialNumber().equals(serialNumber)) {
                return reader;
            }
        }
        return null;
    }
    public void listReaders() {
        for (Reader reader : readers) {
            Log.d("ReaderManager", "Reader: " + reader.getSerialNumber());
        }
    }
}
