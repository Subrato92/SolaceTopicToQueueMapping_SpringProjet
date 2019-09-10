package com.subrato.packages.solace.TopicToQueueMapping.pojos;

import java.io.Serializable;

public class RouterConfig implements Serializable {
	
	private String host;
	private String username;
	private String vpn_name;
	private String password;

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getVpn_name() {
		return vpn_name;
	}
	public void setVpn_name(String vpn_name) {
		this.vpn_name = vpn_name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
