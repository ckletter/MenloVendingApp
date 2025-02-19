package com.example.menlovending;

import junit.framework.TestCase;

import org.junit.Test;

public class ArduinoHelperTest extends TestCase {
    @Test
    public void test() {
        ArduinoHelper ah = new ArduinoHelper();
        ah.connectToArduino();
    }
}