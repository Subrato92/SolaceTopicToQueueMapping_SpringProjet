package com.subrato.packages.solace.TopicToQueueMapping.pojos;

public class TopicPublisherRequest {
    private RouterConfig routerConfig;
    private boolean subscribeToQueue;

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
}
