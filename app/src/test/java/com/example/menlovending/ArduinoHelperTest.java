package com.example.menlovending;

import junit.framework.TestCase;

import org.junit.Test;

import jssc.SerialPortException;

public class ArduinoHelperTest extends TestCase {
    @Test
    public void test() throws SerialPortException {
        ArduinoHelper ah = new ArduinoHelper();
        ah.writeData();
    }
}