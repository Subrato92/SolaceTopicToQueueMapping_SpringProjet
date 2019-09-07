package com.subrato.packages.solace.TopicToQueueMapping.config;

import com.solacesystems.jcsmp.*;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.StatusReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectPublisher {

    private XMLMessageProducer directPublisher = null;
    private Topic topic = null;
    private String topicName = null;
    private TextMessage textMessage = null;
    private MessageRouter router = null;

    private Logger log = LoggerFactory.getLogger(DirectPublisher.class);

    public DirectPublisher (MessageRouter router, String topicName){
        this.router = router;
        this.topicName = topicName;
    }

    public StatusReport initialize(){
        if(router == null || router.getSession() == null || topicName == null){
            return new StatusReport("Router/Session/TopicName is null", false);
        }

        try {
            directPublisher = router.getSession().getMessageProducer(new JCSMPStreamingPublishEventHandler() {
                @Override
                public void handleError(String s, JCSMPException e, long l) {
                    log.info("[ERROR] MessageId: " + s + ", Exception: " + e.getMessage() + ", timestamp: " + l );
                }

                @Override
                public void responseReceived(String s) {
                    log.info("Response Received for MessageId: " + s + ", topic: " + topicName);
                }
            });
        } catch (JCSMPException e) {
            directPublisher = null;
            return new StatusReport(e.getMessage(), false);
        }

        topic = JCSMPFactory.onlyInstance().createTopic(topicName);
        textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);

        return new StatusReport("Successfully Initialized.", true);
    }

    public StatusReport publish(String message){
        if( directPublisher == null || topic == null ){
            return new StatusReport("Publisher/Topic Not Initialized.", false);
        }

        textMessage.setText(message);
        try {
            directPublisher.send(textMessage, topic);
        } catch (JCSMPException e) {
            return new StatusReport(e.getMessage(), false);
        }

        return new StatusReport("Success", true);
    }

    public boolean close(){
        if(directPublisher != null){
            directPublisher.close();
        }

        return true;
    }

}
