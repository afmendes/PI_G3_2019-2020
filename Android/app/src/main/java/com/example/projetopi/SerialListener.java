package com.example.projetopi;

/**
 * ##SerialListener
 *
 * Simple listener for the serial interface for the [SerialService]
 */
interface SerialListener {
    void onSerialConnect();
    void onSerialConnectError (Exception e);
    void onSerialRead(byte[] data);
    void onSerialIoError      (Exception e);
}
