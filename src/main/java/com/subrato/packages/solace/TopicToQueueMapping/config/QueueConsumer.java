package com.subrato.packages.solace.TopicToQueueMapping.config;

import com.solacesystems.jcsmp.*;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.StatusReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class QueueConsumer {

    private MessageRouter router;
    private Queue queue;
    private String QueueName;
    private FlowReceiver cons = null;
    private ConsumerFlowProperties flowProperties;
    private EndpointProperties endpoint_props;
    private CountDownLatch latch;
    private boolean consumerActive;
    private ArrayList<String> messageList = null;

    private Logger log = LoggerFactory.getLogger(QueueConsumer.class);

    public QueueConsumer(MessageRouter router, String QueueName){
        this.router = router;
        this.QueueName = QueueName;
    }

    public StatusReport initialize(){
        if( router==null || QueueName==null ){
            return new StatusReport("Router/QueueName is Null.", false);
        }

        queue = JCSMPFactory.onlyInstance().createQueue(QueueName);
        StatusReport reportOfRouterConnectivity = router.connect(queue, null, null);
        if(!reportOfRouterConnectivity.isStatus()){
            return reportOfRouterConnectivity;
        }

        flowProperties = new ConsumerFlowProperties();
        flowProperties.setEndpoint(queue);
        flowProperties.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);

        endpoint_props = new EndpointProperties();
        endpoint_props.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);

        messageList = new ArrayList<String>();
        latch = new CountDownLatch(1);

        try {
            cons = router.getSession().createFlow(new XMLMessageListener() {
                @Override
                public void onReceive(BytesXMLMessage msg) {
                    if (msg instanceof TextMessage) {
                        log.info("@Queue : TextMessage received: " + ((TextMessage) msg).getText());
                        messageList.add(((TextMessage) msg).getText());
                    } else {
                        log.info("@Queue : Message received...");
                        log.info("@Queue : Message Dump: ");
                        log.info(msg.dump());
                    }

                    // When the ack mode is set to SUPPORTED_MESSAGE_ACK_CLIENT,
                    // guaranteed delivery messages are acknowledged after
                    // processing
                    msg.ackMessage();
                    latch.countDown(); // unblock main thread
                }

                @Override
                public void onException(JCSMPException e) {
                    System.out.printf("@Queue : Consumer received exception: %s%n", e);
                    latch.countDown();
                }
            },flowProperties, endpoint_props);

            cons.start();
            consumerActive = true;

            try {
                latch.await();
            } catch (InterruptedException e) {
                log.info("I was awoken while waiting");
            }
        } catch (JCSMPException e) {
            return new StatusReport("Queue Consumer Initialization Failed: "+e.getMessage(), false);
        }

        return new StatusReport("Success", true);
    }

    public String getMesssages() {
        StringBuilder sb = new StringBuilder();

        sb.append("[RECONCILE MESSAGE] : ");
        sb.append(System.getProperty("line.separator"));

        while (!messageList.isEmpty()) {
            sb.append(messageList.remove(0));
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }

    public void killConnection(){
        if( cons != null ) {
            cons.close();
            cons = null;
        }
        consumerActive = false;
        if(router != null){
            router.kill();
            router = null;
        }
        flowProperties = null;
        endpoint_props = null;
    }

    public boolean isConsumerActive() {
        return consumerActive;
    }
}
