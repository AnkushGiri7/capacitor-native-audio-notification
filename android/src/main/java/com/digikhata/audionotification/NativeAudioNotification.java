package com.digikhata.audionotification;

import com.getcapacitor.Logger;

public class NativeAudioNotification {

    public String echo(String value) {
        Logger.info("Echo", value);
        return value;
    }
}
