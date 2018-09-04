package com.skyland.zht;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UserInfo {

	public UserInfo() {
		// TODO Auto-generated constructor stub
	}
	
    private UserTypeEnum UserType;
    private String UserId="";
    private String AppId="";
    
	public String getAppId() {
		return AppId;
	}
	public void setAppId(String appId) {
		AppId = appId;
	}
	public UserTypeEnum getUserType() {
		return UserType;
	}
	public void setUserType(UserTypeEnum userType) {
		UserType = userType;
	}
    public String getUserId() {
		return UserId;
	}
	public void setUserId(String userId) {
		UserId = userId;
	}
	public void signOut()
	{
		setUserType(UserTypeEnum.NotLogin);
		setUserId("");
		setAppId("");
	}
	
	public boolean isLogin()
	{
		if(UserType==UserTypeEnum.NotLogin)
		{
			return false;
		}
		else if(UserType==UserTypeEnum.Login)
		{
			if(UserId.equals(""))
			{
				return false;
			}
		}
		return true;
	}
	
	public void save(Context context)
	{
		SharedPreferences preferences= context.getSharedPreferences(Config.UserPreferences, Context.MODE_PRIVATE);
		Editor editor= preferences.edit();
		
		editor.putInt(Config.UserType, getUserType().getValue());
		editor.putString(Config.UserId, getUserId());
		editor.putString(Config.AppId, getAppId());
		editor.commit();
	}
	
	public void init(Context context)
	{
		SharedPreferences preferences = context.getSharedPreferences(
				Config.UserPreferences, Context.MODE_PRIVATE);
		
		setUserType(UserTypeEnum.valueOf(preferences.getInt(
				Config.UserType, -1)));
		setUserId(preferences.getString(Config.UserId, ""));
		setUserId(preferences.getString(Config.AppId, ""));
	}
	
	
}
