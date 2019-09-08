package com.subrato.packages.solace.TopicToQueueMapping.pojos;

import com.subrato.packages.solace.TopicToQueueMapping.config.MessageRouter;

public class QueuePubSubInitializer {
    private RouterConfig routerConfig = null;
    private String queueName = null;
    private boolean defaultConfig = false;
    private boolean defaultQueue = false;

    public RouterConfig getRouterConfig() {
        return routerConfig;
    }
    public void setRouterConfig(RouterConfig routerConfig) {
        this.routerConfig = routerConfig;
    }
    public String getQueueName() {
        return queueName;
    }
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
    public boolean isDefaultConfig() {
        return defaultConfig;
    }
    public void setDefaultConfig(boolean defaultConfig) {
        this.defaultConfig = defaultConfig;
    }
    public boolean isDefaultQueue() {
        return defaultQueue;
    }
    public void setDefaultQueue(boolean defaultQueue) {
        this.defaultQueue = defaultQueue;
    }
}
