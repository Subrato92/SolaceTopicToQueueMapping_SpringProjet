package com.subrato.packages.solace.TopicToQueueMapping.pojos;

public class DefaultRouterConfig {

    private RouterConfig config;

    public DefaultRouterConfig(RouterConfig config){
        this.config = config;
    }

    public RouterConfig getConfig() {
        return config;
    }
}
