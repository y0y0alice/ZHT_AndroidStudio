package com.skyland.zht;

public enum UserTypeEnum {

	NotLogin(-1),
	Login(0);

	private int value = 0;

	private UserTypeEnum(int value) {
		this.value = value;
	}
	
	public int getValue()
	{
		return this.value;
	}

	public static UserTypeEnum valueOf(int value) {
		switch (value) {
		case -1:
			return NotLogin;
		case 0:
			return Login;
		default:
			return null;
		}
	}
	
}
