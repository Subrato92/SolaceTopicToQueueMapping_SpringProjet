package com.subrato.packages.solace.TopicToQueueMapping.config;

import com.solacesystems.jcsmp.*;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.RouterConfig;
import com.subrato.packages.solace.TopicToQueueMapping.pojos.StatusReport;

public class MessageRouter {
	
	private JCSMPProperties properties;
	private JCSMPSession session = null;
	
	public MessageRouter(RouterConfig config) {
		properties = new JCSMPProperties();
		
		properties.setProperty(JCSMPProperties.HOST, config.getHost());
		properties.setProperty(JCSMPProperties.USERNAME, config.getUsername());
		properties.setProperty(JCSMPProperties.PASSWORD, config.getPassword());
		properties.setProperty(JCSMPProperties.VPN_NAME, config.getVpn_name());
	}

	private StatusReport addProvisionForQueue(Queue queue){
		EndpointProperties endpointProps = new EndpointProperties();
		endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
		endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_EXCLUSIVE);

		try {
			session.provision(queue, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
		} catch (JCSMPException e) {
			return new StatusReport("Failed To Add Provision: " + e.getMessage(), true);
		}

		return new StatusReport("Success", true);
	}

	public StatusReport connect(Queue queue, String topicToQueue, Topic topic) {
		String response = "";
		boolean status = false;

		if( session != null ){
			response = "Session Already Active.";
			return new StatusReport(response, true);
		}

		//--- IMPROVISATION (1) : Prop to avoid exception or Error on mapping an existing topic to queue
		if(topicToQueue != null && topic != null) {
			properties.setProperty(JCSMPProperties.IGNORE_DUPLICATE_SUBSCRIPTION_ERROR, true);
		}

		try {
			session = JCSMPFactory.onlyInstance().createSession(properties);
			response = "Session created with properties... ";

			if(queue != null){										//--- IMPROVISATION (2) : Session For Queue Pub-Sub
				addProvisionForQueue(queue);
				response = response.concat("Registering queue for persistance... ");
			}else if(topicToQueue != null && topic != null){		//--- IMPROVISATION (3) : Registering Topic to Queue

				StatusReport report = checkSessionEligibility();
				if(!report.isStatus()){
					return report;
				}

				Queue persistingQueue = JCSMPFactory.onlyInstance().createQueue(topicToQueue);
				session.addSubscription(persistingQueue, topic, JCSMPSession.WAIT_FOR_CONFIRM);
				response = response.concat("Topic Registered to queue 'topicToQueue'... ");
			}

			session.connect();
			response = response.concat("Success");
			status = true;
		} catch (InvalidPropertiesException e) {
			session = null;
			response = "Session Instance Creation Failed - " + e.getMessage();
		} catch (JCSMPErrorResponseException e){
			session = null;
			response = "Failed To Connect " + e.getMessage();
		}
		catch (JCSMPException e) {
			session = null;
			response = "Failed To Connect " + e.getMessage();
		}
		
		return new StatusReport(response, status);
	}

	public StatusReport kill(){
		if( session != null ){
			session.closeSession();
			session = null;
			return new StatusReport("Session Closed", true);
		}

		return new StatusReport("Session not initialized.", true);
	}

	public JCSMPProperties getProperties() {
		return properties;
	}
	public JCSMPSession getSession() {
		return session;
	}

	private StatusReport checkSessionEligibility(){
		String response = "";

		if (session.isCapable(CapabilityType.PUB_GUARANTEED) &&
				session.isCapable(CapabilityType.SUB_FLOW_GUARANTEED) &&
				session.isCapable(CapabilityType.ENDPOINT_MANAGEMENT) &&
				session.isCapable(CapabilityType.QUEUE_SUBSCRIPTIONS)) {
				response = "All required capabilities supported!";

				return new StatusReport(response, false);
		}

		response = "Capabilities not met!";

		return new StatusReport(response, true);
	}
}
