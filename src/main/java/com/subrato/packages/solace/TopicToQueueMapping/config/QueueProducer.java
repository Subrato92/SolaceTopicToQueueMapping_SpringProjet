package com.subrato.packages.solace.TopicToQueueMapping.config;

import com.solacesystems.jcsmp.*;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.MessageInfo;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.StatusReport;
import com.subrato.packages.solace.TopicToQueueMapping.utilities.PublisherCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;

public class QueueProducer {

    private LinkedList<MessageInfo> msgList = new LinkedList<MessageInfo>();
    private XMLMessageProducer prod = null;
    private MessageRouter router = null;
    private CountDownLatch latch = null;
    private String queueName = null;
    private Queue queue = null;
    private boolean sessionActive = false;
    private int id = 0;

    public QueueProducer(MessageRouter router, String queueName){
        this.router = router;
        this.queueName = queueName;
    }

    private Logger log = LoggerFactory.getLogger(Producer.class);

    public StatusReport initialize() throws JCSMPException {

        if(router == null || queueName == null){
            return new StatusReport("Router/QueueName is null", false);
        }

        queue = JCSMPFactory.onlyInstance().createQueue(queueName);
        StatusReport reportOfRouterConnectivity = router.connect(queue);

        if( !reportOfRouterConnectivity.isStatus() ) {
            return reportOfRouterConnectivity;
        }
        sessionActive = true;

        this.latch = new CountDownLatch(1);;
        prod = router.getSession().getMessageProducer(new PublisherCallback(latch));

        return new StatusReport("Success", true);
    }

    public String sendMsg(String message) {

        TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        msg.setDeliveryMode(DeliveryMode.PERSISTENT);
        msg.setText(message);

        // Delivery not yet confirmed.
        final MessageInfo msgCorrelationInfo = new MessageInfo(++id);
        msgCorrelationInfo.sessionIndependentMessage = msg;
        msgList.add(msgCorrelationInfo);
        msg.setCorrelationKey(msgCorrelationInfo);

        try {
            prod.send(msg, queue);
        } catch (JCSMPException e) {
            return "[Publishing Failed] " + e.getMessage();
        }

        try {
            latch.await(); // block here until message received, and latch will flip
        } catch (InterruptedException e) {
            System.out.println("I was awoken while waiting");
        }

        return "[Message Pushed]";
    }

    public String reconsile(){
        StringBuilder sb = new StringBuilder();

        while (msgList.peek() != null) {
            final MessageInfo ackedMsgInfo = msgList.poll();
            String resp = "Removing acknowledged message (%s) from application list.\n" + ackedMsgInfo;

            sb.append(resp);
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }

    public void close(){

        sessionActive = false;

        if(prod != null ) {
            prod.close();
            prod = null;
            log.info("Producer Closing...");
        }else{
            log.info("Producer Is Not Initalized...");
        }

        if(router != null){
            router.kill();
            router = null;
        }

        if(latch != null){
            latch = null;
        }

        if(!msgList.isEmpty()){
            String msg = reconsile();
            log.info("Final Reconcile Messages: ");
            log.info(msg);
        }

    }


}
