package com.subrato.packages.solace.TopicToQueueMapping.utilities;

import com.subrato.packages.solace.TopicToQueueMapping.pojos.MessagePayload;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.RouterConfig;

public class Validator {

    public static boolean stringValidator(String aString) {
        if (aString == null) {
            return false;
        }
        return true;
    }

    public static boolean messagePayloadValidator(MessagePayload payload) {
        if (payload == null || payload.getRef() == null || payload.getMessage() == null) {
            return false;
        }
        return true;
    }

    public static boolean checkForValidity(RouterConfig config) {

        if (config == null || config.getHost() == null || config.getPassword() == null || config.getUsername() == null || config.getVpn_name() == null) {
            return false;
        }
        return true;
    }

}
