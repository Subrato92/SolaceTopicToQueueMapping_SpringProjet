package com.subrato.packages.solace.TopicToQueueMapping.pojos;

public class TopicPublisherRequest {
    private RouterConfig routerConfig;
    private String topicName;
    private boolean defaultConfig = false;
    private boolean subscribeToQueue = false;

    public RouterConfig getRouterConfig() {
        return routerConfig;
    }
    public void setRouterConfig(RouterConfig routerConfig) {
        this.routerConfig = routerConfig;
    }
    public boolean isSubscribeToQueue() {
        return subscribeToQueue;
    }
    public void setSubscribeToQueue(boolean subscribeToQueue) {
        this.subscribeToQueue = subscribeToQueue;
    }
    public String getTopicName() {
        return topicName;
    }
    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
    public boolean isDefaultConfig() {
        return defaultConfig;
    }
    public void setDefaultConfig(boolean defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

}
