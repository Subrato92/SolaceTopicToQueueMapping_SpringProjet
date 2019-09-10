package com.subrato.packages.solace.TopicToQueueMapping.constants;

import com.subrato.packages.solace.TopicToQueueMapping.pojos.RouterConfig;

public enum Config {
    STATIC;

    private RouterConfig routerConfig;
    private String defaultQueueName;

    public RouterConfig getRouterConfig() {
        return routerConfig;
    }
    public void setRouterConfig(RouterConfig routerConfig) {
        this.routerConfig = routerConfig;
    }
    public String getDefaultQueueName() {
        return defaultQueueName;
    }
    public void setDefaultQueueName(String defaultQueueName) {
        this.defaultQueueName = defaultQueueName;
    }
}
