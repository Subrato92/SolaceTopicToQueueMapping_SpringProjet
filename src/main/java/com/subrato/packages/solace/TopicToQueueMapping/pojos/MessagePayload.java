package com.subrato.packages.solace.TopicToQueueMapping.pojos;

public class MessagePayload {
    private String ref;
    private String message;

    public String getRef() {
        return ref;
    }
    public void setRef(String ref) {
        this.ref = ref;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
