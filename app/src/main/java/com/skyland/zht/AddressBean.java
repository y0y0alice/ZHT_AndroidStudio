package com.skyland.zht;

public class AddressBean {
	private String URL;
	private String Name;

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return Name;
	}
}
