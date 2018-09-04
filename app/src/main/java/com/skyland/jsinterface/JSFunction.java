package com.skyland.jsinterface;

import android.net.Uri;
import com.tencent.smtt.sdk.WebView;

public class JSFunction {

	String funcID;
	boolean removeAfterExecute;
	WebView webView;
	
	public void setWebView(WebView webView) {
		this.webView = webView;
	}
	public String getFuncID() {
		return funcID;
	}
	public void setFuncID(String funcID) {
		this.funcID = funcID;
	}
	public boolean isRemoveAfterExecute() {
		return removeAfterExecute;
	}
	public void setRemoveAfterExecute(boolean removeAfterExecute) {
		this.removeAfterExecute = removeAfterExecute;
	}

	public void executeWithParams(String... args)
	{
		StringBuilder builder=new StringBuilder();
		builder.append("BridgeJS.invokeCallback(\'"+this.funcID+"\',"+(this.removeAfterExecute?"1":"0"));
		for(int i=0;i<args.length;i++)
		{
			String arg =args[i];
			builder.append(",\'"+Uri.encode(arg)+"\'");
		}
		builder.append(");");
		webView.loadUrl("javascript:" + builder.toString());
	}
}
