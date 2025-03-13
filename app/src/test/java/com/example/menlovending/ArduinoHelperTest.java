package com.example.menlovending;

import com.example.menlovending.stripe.manager.ArduinoHelper;
import com.example.menlovending.stripe.manager.ArduinoHelperMac;

import junit.framework.TestCase;

import org.junit.Test;

import jssc.SerialPortException;

public class ArduinoHelperTest extends TestCase {
    @Test
    public void test() throws SerialPortException {
        ArduinoHelperMac ah = new ArduinoHelperMac();
        ah.writeData();
    }
}