package com.subrato.packages.solace.TopicToQueueMapping.pojos;

import com.solacesystems.jcsmp.JCSMPSession;

public class SessionResponse {
	private JCSMPSession session = null;
	private String response;
	
	public SessionResponse(JCSMPSession session, String response) {
		this.session = session;
		this.response = response;
	}

	public JCSMPSession getSession() {
		return session;
	}

	public String getResponse() {
		return response;
	}
	
}
