package com.subrato.packages.solace.TopicToQueueMapping.controller;

import com.subrato.packages.solace.TopicToQueueMapping.config.*;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.print.attribute.standard.Media;
import java.util.HashMap;

@RestController
@EnableSwagger2
@RequestMapping( value = "/topic-to-queue-mapping")
public class RestEndPoints {

    private RouterConfig defaultRouterConfig = null;
    private String defaultQueueName = null;
    private HashMap<String, DirectPublisher> lstOfDirectPublishers = new HashMap<String, DirectPublisher>();
    private HashMap<String, DirectConsumer> lstOfDirectConsumers = new HashMap<String, DirectConsumer>();
    private HashMap<String, QueueProducer> lstOfQueuePublishers = new HashMap<String, QueueProducer>();
    private HashMap<String, QueueConsumer> lstOfQueueConsumers = new HashMap<String, QueueConsumer>();

    private Logger log = LoggerFactory.getLogger(RestEndPoints.class);

    @PostMapping(
            value = "/routerconfig/setdefault",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody StatusReport setDefaultRouterConfig(@RequestBody RouterConfig config){
        if(!checkForValidity(config)) {
            return new StatusReport("Parameters Missing...", false);
        }
        defaultRouterConfig = config;
        return new StatusReport("Success", true);
    }

    @GetMapping(
            value = "/routerconfig/getDefault",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody RouterConfig getDefaultRouterConfig(){
        return defaultRouterConfig;
    }

    @PostMapping(
            value = "/queue/setdefault",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody boolean setDefaultQueue(@RequestBody String defaultQueueName){

        this.defaultQueueName = defaultQueueName;
        return true;
    }

    @GetMapping(
            value = "/queue/getdefault",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody String getDefaultQueue(){
        return defaultQueueName;
    }

    @PostMapping(
            value = "/topicpubsub/add",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody StatusReport createTopicPubSub(@RequestBody TopicPublisherRequest payload){

        if( payload.getRouterConfig().getTopicName() == null ){
            return new StatusReport("[MissingTopicName]", false);
        }
        MessageRouter router = null;
        boolean isValid = checkForValidity(payload.getRouterConfig());
        DirectPublisher publisher = null;
        DirectConsumer consumer = null;

        if( !isValid && defaultRouterConfig == null ){
            return new StatusReport("Fields Missing. No Default Router Set.", false);
        }else if(isValid){
            router = new MessageRouter(payload.getRouterConfig());
        }else if(defaultRouterConfig != null){
            router = new MessageRouter(defaultRouterConfig);
        }

        if(payload.isSubscribeToQueue() && defaultQueueName == null){
            return new StatusReport("Default Queue Missing ...", false);
        }else if( !payload.isSubscribeToQueue() ){
            publisher = new DirectPublisher(router, payload.getRouterConfig().getTopicName(), null);
        }else {
            publisher = new DirectPublisher(router, payload.getRouterConfig().getTopicName(), defaultQueueName);
        }

        consumer = new DirectConsumer(router, payload.getRouterConfig().getTopicName());
        StatusReport pubInitializerReport = null;
        StatusReport subInitializerReport = null;

        if(publisher != null){
            log.info("Initializing Topic Publisher...");
            pubInitializerReport = publisher.initialize();

            if(pubInitializerReport.isStatus()) {
                lstOfDirectPublishers.put(payload.getRouterConfig().getTopicName(), publisher);
            }else{
                return pubInitializerReport;
            }
        }

        if(consumer != null){
            log.info("Initializing Topic Consumer...");
            subInitializerReport = consumer.initialize();

            if(subInitializerReport.isStatus()){
                lstOfDirectConsumers.put(payload.getRouterConfig().getTopicName(), consumer);
            }else{
                publisher.close();
                lstOfDirectPublishers.remove(payload.getRouterConfig().getTopicName());
                return subInitializerReport;
            }
        }

        return new StatusReport("Success", true);
    }

    @PostMapping(
            value = "/queuepubsub/add",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody StatusReport createQueuePubSub(@RequestBody QueuePubSubInitializer payload){

        if( payload.getRouterConfig() == null && !payload.isDefaultConfig() ){
            return new StatusReport("[MissingRouterConfig]", false);
        }else if(payload.isDefaultConfig() && defaultRouterConfig == null ) {
            return new StatusReport("DefaultRouterConfig Missing. Add Default Router Config.", false);
        }

        if(payload.getQueueName() == null && !payload.isDefaultQueue()){
            return new StatusReport("QueueName Missing.", false);
        }

        MessageRouter router = null;
        if(payload.isDefaultConfig()){
            router = new MessageRouter(defaultRouterConfig);
        }else{
            boolean isValid = checkForValidity(payload.getRouterConfig());
            if(isValid){
                router = new MessageRouter(payload.getRouterConfig());
            }else{
                return new StatusReport("Router Config Fields Missing.", false);
            }
        }

        QueueProducer publisher = null;
        QueueConsumer consumer = null;

        if(payload.isDefaultQueue()){
            publisher = new QueueProducer(router, defaultQueueName);
            consumer = new QueueConsumer(router, defaultQueueName);
        }else{
            publisher = new QueueProducer(router, payload.getQueueName());
            consumer = new QueueConsumer(router, payload.getQueueName());
        }

        StatusReport publisherInitializerStatus;
        StatusReport consumerInitializerStatus;

        if(publisher != null){
            log.info("Initializing Queue Publisher...");
            publisherInitializerStatus = publisher.initialize();

            if(publisherInitializerStatus.isStatus() && payload.isDefaultQueue()){
                lstOfQueuePublishers.put(defaultQueueName, publisher);
            }else if( publisherInitializerStatus.isStatus() ){
                lstOfQueuePublishers.put(payload.getQueueName(), publisher);
            }else{
                return publisherInitializerStatus;
            }
        }

        if(consumer != null){
            log.info("Initializing Queue Consumer...");
            consumerInitializerStatus = consumer.initialize();

            if(consumerInitializerStatus.isStatus() && payload.isDefaultQueue()){
                lstOfQueueConsumers.put(defaultQueueName, consumer);
            }else if( consumerInitializerStatus.isStatus() ){
                lstOfQueueConsumers.put(payload.getQueueName(), consumer);
            }else{
                lstOfQueuePublishers.remove(payload.getQueueName());
                return consumerInitializerStatus;
            }
        }

        return new StatusReport("Success", true);
    }


    @PostMapping(
            value = "topic/sendmsg",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody StatusReport sendMessageToTopic(@RequestBody MessagePayload message){

        if( message.getTopic() == null ){
            return new StatusReport("Topic Null", false);
        }else if(message.getMessage() == null){
            return new StatusReport("Message Null", false);
        }

        if(lstOfDirectPublishers.containsKey(message.getTopic())){
            DirectPublisher publisher = lstOfDirectPublishers.get(message.getTopic());
            publisher.publish(message.getMessage());
        }else{
            return new StatusReport("No Publisher For the Topic...", false);
        }

        return new StatusReport("Success", true);
    }

    public @ResponseBody StatusReport getMessageFromTopic(){
        return new StatusReport("Success", true);
    }

    public @ResponseBody StatusReport sendMessageToQueue(){
        return new StatusReport("Success", true);
    }

    public @ResponseBody StatusReport getMessageFromQueue(){
        return new StatusReport("Success", true);
    }

    private boolean checkForValidity(RouterConfig config){
        if( config.getHost() == null || config.getPassword() == null || config.getUsername() == null || config.getVpn_name() == null ){
            return false;
        }

        return true;
    }

}
