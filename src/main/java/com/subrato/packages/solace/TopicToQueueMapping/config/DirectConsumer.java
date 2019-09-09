package com.subrato.packages.solace.TopicToQueueMapping.config;

import com.solacesystems.jcsmp.*;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.MessageStructure;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.StatusReport;

import java.util.ArrayList;

public class DirectConsumer {

    private XMLMessageConsumer consumer = null;
    private MessageRouter router = null;
    private String topicName = null;
    private Topic topic = null;
    private ArrayList<MessageStructure> lst;

    public DirectConsumer(MessageRouter router, String topicName){
        this.router = router;
        this.topicName = topicName;
        lst = new ArrayList<MessageStructure>();
    }

    public StatusReport initialize(){

        if( router != null || router.getSession() != null || topicName == null){
            return new StatusReport("Router/Session/TopicName is null", false);
        }

        try {
            consumer = router.getSession().getMessageConsumer(new XMLMessageListener() {
                @Override
                public void onReceive(BytesXMLMessage bytesXMLMessage) {
                    if( bytesXMLMessage instanceof TextMessage){
                        lst.add(new MessageStructure(((TextMessage)bytesXMLMessage).getText(),false));
                    }else{
                        lst.add(new MessageStructure(bytesXMLMessage.dump(),false));
                    }
                }

                @Override
                public void onException(JCSMPException e) {
                    lst.add(new MessageStructure(e.getMessage(), true));
                }
            });

            topic = JCSMPFactory.onlyInstance().createTopic(topicName);
            router.getSession().addSubscription(topic);
            consumer.start();

        } catch (JCSMPException e) {
            consumer = null;
            return new StatusReport(e.getMessage(), false);
        }

        return new StatusReport("Successfully Initialized.", true);
    }

    public String getMessage() {

        StringBuilder sb = new StringBuilder();

        sb.append("[MESSAGE] : ");

        while(!lst.isEmpty()){
            sb.append(lst.remove(0).toString());
            sb.append(System.getProperty("line.separator"));
        }

        return sb.toString();
    }

    public void closeConnection(){
        // Close consumer
        consumer.close();
    }

}
