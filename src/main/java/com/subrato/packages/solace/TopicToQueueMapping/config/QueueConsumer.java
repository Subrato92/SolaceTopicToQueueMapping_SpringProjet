package com.subrato.packages.solace.TopicToQueueMapping.config;

import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.Queue;

public class QueueConsumer {

    private MessageRouter router;
    private Queue queue;
    private String QueueName;
    private ConsumerFlowProperties flowProperties;
    private EndpointProperties endPtProp;
    private boolean sessionActive;

    public QueueConsumer(MessageRouter router, String QueueName){
        this.router = router;
        this.QueueName = QueueName;
    }



}
