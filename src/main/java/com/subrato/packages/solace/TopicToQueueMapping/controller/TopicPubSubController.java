package com.subrato.packages.solace.TopicToQueueMapping.controller;

import com.subrato.packages.solace.TopicToQueueMapping.config.DirectConsumer;
import com.subrato.packages.solace.TopicToQueueMapping.config.DirectPublisher;
import com.subrato.packages.solace.TopicToQueueMapping.config.MessageRouter;
import com.subrato.packages.solace.TopicToQueueMapping.constants.Config;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.MessagePayload;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.RouterConfig;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.StatusReport;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.TopicPublisherRequest;
import com.subrato.packages.solace.TopicToQueueMapping.utilities.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashMap;

@RestController
@EnableSwagger2
@RequestMapping(value = "/topicToQueueMapping/topic")
public class TopicPubSubController {

    private HashMap<String, DirectPublisher> lstOfDirectPublishers = new HashMap<String, DirectPublisher>();
    private HashMap<String, DirectConsumer> lstOfDirectConsumers = new HashMap<String, DirectConsumer>();

    private Logger log = LoggerFactory.getLogger(TopicPubSubController.class);

    @PostMapping(
            value = "/add-pubsub",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody
    StatusReport createTopicPubSub(@RequestBody TopicPublisherRequest payload) {

        RouterConfig defaultRouterConfig = Config.STATIC.getRouterConfig();
        String defaultQueueName = Config.STATIC.getDefaultQueueName();

        if (payload.getTopicName() == null) {
            return new StatusReport("[MissingTopicName]", false);
        }
        MessageRouter router = null;
        boolean isValid = Validator.checkForValidity(payload.getRouterConfig());
        DirectPublisher publisher = null;
        DirectConsumer consumer = null;

        //--- Router : Check For Default/Custom config
        if(payload.isDefaultConfig()){
            if( defaultRouterConfig == null ){
                return new StatusReport("No Default Router Set.", false);
            }
            router = new MessageRouter(defaultRouterConfig);
        }
        else{
            if( !isValid ){
                return new StatusReport("Config field(s) Invalid. ", false);
            }
            router = new MessageRouter(payload.getRouterConfig());
        }

        //--- PublisherInitialization : Check For Subscribing Topic to Queue
        if (payload.isSubscribeToQueue() ) {
            if( defaultQueueName == null ) {
                return new StatusReport("Default Queue Missing ... Failed To Initialize.", false);
            }
            publisher = new DirectPublisher(router, payload.getTopicName(), defaultQueueName);

        }
        else {
            publisher = new DirectPublisher(router, payload.getTopicName(), null);
        }

        consumer = new DirectConsumer(router, payload.getTopicName());
        StatusReport pubInitializerReport = null;
        StatusReport subInitializerReport = null;

        if (publisher != null) {
            log.info("Initializing Topic Publisher...");
            pubInitializerReport = publisher.initialize();

            if (pubInitializerReport.isStatus()) {
                lstOfDirectPublishers.put(payload.getTopicName(), publisher);
            } else {
                return pubInitializerReport;
            }
        }

        if (consumer != null) {
            log.info("Initializing Topic Consumer...");
            subInitializerReport = consumer.initialize();

            if (subInitializerReport.isStatus()) {
                lstOfDirectConsumers.put(payload.getTopicName(), consumer);
            } else {
                publisher.close();
                lstOfDirectPublishers.remove(payload.getTopicName());
                return subInitializerReport;
            }
        }

        return new StatusReport("Success", true);
    }

    @PostMapping(
            value = "/sendmsg",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody
    StatusReport sendMessageToTopic(@RequestBody MessagePayload message) {

        if (!Validator.messagePayloadValidator(message)) {
            return new StatusReport("Topic/Message Invalid", false);
        }

        if (lstOfDirectPublishers.containsKey(message.getRef())) {
            DirectPublisher publisher = lstOfDirectPublishers.get(message.getRef());
            publisher.publish(message.getMessage());
        } else {
            return new StatusReport("No Publisher For the Topic...", false);
        }

        return new StatusReport("Success", true);
    }

    @PostMapping(
            value = "/getmsg",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody
    StatusReport getMessageFromTopic(@RequestBody String topicName) {

        if (topicName == null) {
            return new StatusReport("Inavlid Topic.", false);
        }

        DirectConsumer consumer = lstOfDirectConsumers.get(topicName);
        String messages = consumer.getMessage();

        return new StatusReport(messages, true);
    }


}
