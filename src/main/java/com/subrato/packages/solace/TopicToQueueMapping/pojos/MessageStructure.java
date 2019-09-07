package com.subrato.packages.solace.TopicToQueueMapping.pojos;

import java.time.LocalDateTime;

public class MessageStructure {
    private boolean error ;
    private LocalDateTime ldt ;
    private String message ;

    public MessageStructure(String message, boolean error){
        this.message = message;
        this.error = error;
        ldt = LocalDateTime.now();
    }

    public boolean isError() {
        return error;
    }
    public LocalDateTime getLdt() {
        return ldt;
    }
    public String getMessage() {
        return message;
    }

    @Override
    public String toString(){
        if(error){
            if(message != null) {
                return "[" + ldt.toString() + "] Error: " + message;
            }else{
                return "[" + ldt.toString() + "] Error: null";
            }
        }

        return "[" + ldt.toString() + "] Msg: " + message;
    }
}
