package com.example.menlovending.stripe.manager;

import static jssc.SerialPort.BAUDRATE_115200;
import static jssc.SerialPort.DATABITS_8;
import static jssc.SerialPort.PARITY_NONE;
import static jssc.SerialPort.STOPBITS_1;

import jssc.*;

public class ArduinoHelperMac {
    SerialPort port;
    public ArduinoHelperMac() throws SerialPortException {
        String[] portNames = SerialPortList.getPortNames();
        for (String portName : portNames) {
            System.out.println("Available port: " + portName);
        }
        port = new jssc.SerialPort("/dev/tty.usbmodem4827E2E6471C2");
        port.openPort();
        port.setParams(BAUDRATE_115200,  DATABITS_8, STOPBITS_1, PARITY_NONE);
    }
    public void writeData() throws SerialPortException {
        port.writeBytes("DISPENSE".getBytes());
        port.closePort();
    }
}