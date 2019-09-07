package com.subrato.packages.solace.TopicToQueueMapping.pojos;

import jdk.net.SocketFlow;

public class StatusReport {

    private String log = "";
    private boolean status;

    public StatusReport(String log, boolean status){
        this.log = log;
        this.status = status;
    }

    public String getLog() {
        return log;
    }
    public boolean isStatus() {
        return status;
    }

}
