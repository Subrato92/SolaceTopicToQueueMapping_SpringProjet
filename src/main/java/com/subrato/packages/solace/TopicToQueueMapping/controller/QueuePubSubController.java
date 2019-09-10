package com.subrato.packages.solace.TopicToQueueMapping.controller;

import com.subrato.packages.solace.TopicToQueueMapping.config.MessageRouter;
import com.subrato.packages.solace.TopicToQueueMapping.config.QueueConsumer;
import com.subrato.packages.solace.TopicToQueueMapping.config.QueueProducer;
import com.subrato.packages.solace.TopicToQueueMapping.constants.Config;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.MessagePayload;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.QueuePubSubInitializer;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.RouterConfig;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.StatusReport;
import com.subrato.packages.solace.TopicToQueueMapping.utilities.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.HashMap;

@RestController
@EnableSwagger2
@RequestMapping(value = "/topicToQueueMapping/queue")
public class QueuePubSubController {

    private HashMap<String, QueueProducer> lstOfQueuePublishers = new HashMap<String, QueueProducer>();
    private HashMap<String, QueueConsumer> lstOfQueueConsumers = new HashMap<String, QueueConsumer>();

    private Logger log = LoggerFactory.getLogger(QueuePubSubController.class);

    @PostMapping(
            value = "/add-pubsub",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody
    StatusReport createQueuePubSub(@RequestBody QueuePubSubInitializer payload) {

        RouterConfig defaultRouterConfig = Config.STATIC.getRouterConfig();
        String defaultQueueName = Config.STATIC.getDefaultQueueName();

        if (payload.getRouterConfig() == null && !payload.isDefaultConfig()) {
            return new StatusReport("[MissingRouterConfig]", false);
        } else if (payload.isDefaultConfig() && defaultRouterConfig == null) {
            return new StatusReport("DefaultRouterConfig Missing. Add Default Router Config.", false);
        }

        if (payload.getQueueName() == null && !payload.isDefaultQueue()) {
            return new StatusReport("QueueName Missing.", false);
        }

        MessageRouter router = null;
        if (payload.isDefaultConfig()) {
            router = new MessageRouter(defaultRouterConfig);
        } else {
            boolean isValid = Validator.checkForValidity(payload.getRouterConfig());
            if (isValid) {
                router = new MessageRouter(payload.getRouterConfig());
            } else {
                return new StatusReport("Router Config Fields Missing.", false);
            }
        }

        QueueProducer publisher = null;
        QueueConsumer consumer = null;

        if (payload.isDefaultQueue()) {
            publisher = new QueueProducer(router, defaultQueueName);
            consumer = new QueueConsumer(router, defaultQueueName);
        } else {
            publisher = new QueueProducer(router, payload.getQueueName());
            consumer = new QueueConsumer(router, payload.getQueueName());
        }

        StatusReport publisherInitializerStatus;
        StatusReport consumerInitializerStatus;

        if (publisher != null) {
            log.info("Initializing Queue Publisher...");
            publisherInitializerStatus = publisher.initialize();

            if (publisherInitializerStatus.isStatus() && payload.isDefaultQueue()) {
                lstOfQueuePublishers.put(defaultQueueName, publisher);
            } else if (publisherInitializerStatus.isStatus()) {
                lstOfQueuePublishers.put(payload.getQueueName(), publisher);
            } else {
                return publisherInitializerStatus;
            }
        }

        if (consumer != null) {
            log.info("Initializing Queue Consumer...");
            consumerInitializerStatus = consumer.initialize();

            if (consumerInitializerStatus.isStatus() && payload.isDefaultQueue()) {
                lstOfQueueConsumers.put(defaultQueueName, consumer);
            } else if (consumerInitializerStatus.isStatus()) {
                lstOfQueueConsumers.put(payload.getQueueName(), consumer);
            } else {
                lstOfQueuePublishers.remove(payload.getQueueName());
                return consumerInitializerStatus;
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
    StatusReport sendMessageToQueue(@RequestBody MessagePayload payload) {

        if (!Validator.messagePayloadValidator(payload)) {
            return new StatusReport("Message/Queue Invalid.", false);
        }

        if (!lstOfQueuePublishers.containsKey(payload.getRef())) {
            return new StatusReport("No Publisher For Queue-" + payload.getRef(), false);
        }

        QueueProducer producer = lstOfQueuePublishers.get(payload.getRef());
        producer.sendMsg(payload.getMessage());

        return new StatusReport("Success", true);
    }

    @PostMapping(
            value = "/getmsg",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody
    StatusReport getMessageFromQueue(@RequestBody String queueName) {

        if (!Validator.stringValidator(queueName)) {
            return new StatusReport("Invalid Queue Name", false);
        }

        if (!lstOfQueueConsumers.containsKey(queueName)) {
            return new StatusReport("Consumer for queue-" + queueName + " doesn't exist.", false);
        }

        QueueConsumer consumer = lstOfQueueConsumers.get(queueName);

        return new StatusReport(consumer.getMesssages(), true);
    }


}
