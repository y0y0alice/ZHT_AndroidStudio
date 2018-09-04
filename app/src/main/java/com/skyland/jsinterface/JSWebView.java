package com.skyland.jsinterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import com.tencent.smtt.sdk.WebViewClient;
import com.tencent.smtt.sdk.WebView;

public class JSWebView extends WebView {

	private final String injectjs = "window.BridgeJS = {__callbacks: {},invokeCallback: function(cbID, removeAfterExecute){var args = Array.prototype.slice.call(arguments);args.shift();args.shift();for (var i = 0,l = args.length; i < l; i++) {args[i] = decodeURIComponent(args[i]);}var cb = BridgeJS.__callbacks[cbID];if (removeAfterExecute) {BridgeJS.__callbacks[cbID] = undefined;}return cb.apply(null, args);},call: function(obj, functionName, args) {var formattedArgs = [];for (var i = 0,l = args.length; i < l; i++) {if (typeof args[i] == \"function\"){formattedArgs.push(\"f\");var cbID = \"__cb\" + encodeURIComponent(functionName);BridgeJS.__callbacks[cbID] = args[i];formattedArgs.push(cbID);}else{formattedArgs.push(\"s\");formattedArgs.push(encodeURIComponent(JSON.stringify(args[i])));}}var argStr = (formattedArgs.length > 0 ? \":\" + encodeURIComponent(formattedArgs.join(\":\")) : \"\");var iframe = document.createElement(\"IFRAME\");iframe.setAttribute(\"src\", \"BridgeJS:\" + obj + \":\" + encodeURIComponent(functionName) + argStr);document.documentElement.appendChild(iframe);iframe.parentNode.removeChild(iframe);iframe = null;var ret = BridgeJS.retValue;BridgeJS.retValue = undefined;if (ret){return decodeURIComponent(ret);}},inject: function (obj, methods){window[obj] = {};var jsObj = window[obj];for (var i = 0, l = methods.length; i < l; i++){(function (){var method = methods[i];var jsMethod = method.replace(new RegExp(\":\", \"g\"), \"\");jsObj[jsMethod] = function (){return BridgeJS.call(obj, method, Array.prototype.slice.call(arguments));};})();}}};";
	private JSBridge instance = null;
	boolean isRedirected = false;

	public JSWebView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public JSWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public JSWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public JSWebView(Context context, AttributeSet attrs, int defStyle,
			boolean privateBrowsing) {
		super(context, attrs, defStyle, privateBrowsing);

	}

	public void addJavascriptInterface(JSBridge JsBridge) {
		instance = JsBridge;
	}

	WebViewClient realClient;
	DelegateClient delegateClient = new DelegateClient();

	@Override
	public void setWebViewClient(WebViewClient client) {
		// TODO Auto-generated method stub
		realClient = client;
		super.setWebViewClient(delegateClient);
	}

	private class DelegateClient extends WebViewClient {
		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			/*
			 * if(!isRedirected) { if (instance != null) {
			 * view.loadUrl("javascript:" + injectjs); Method[] meths =
			 * instance.getClass().getMethods(); JSONArray array = new
			 * JSONArray(); for (int i = 0; i < meths.length; i++) {
			 * array.put(meths[i].getName()); } String inject =
			 * "BridgeJS.inject(\"JSBridge\", " + array.toString() + ");";
			 * view.loadUrl("javascript:" + inject); } }
			 */
			if (realClient != null) {
				realClient.onPageFinished(view, url);
			}
			super.onPageFinished(view, url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
			/*
			 * if (instance != null) { view.loadUrl("javascript:" + injectjs);
			 * Method[] meths = instance.getClass().getMethods(); JSONArray
			 * array = new JSONArray(); for (int i = 0; i < meths.length; i++) {
			 * array.put(meths[i].getName()); } String inject =
			 * "BridgeJS.inject(\"JSBridge\", " + array.toString() + ");";
			 * view.loadUrl("javascript:" + inject); }
			 */
			if (realClient != null) {
				realClient.onPageStarted(view, url, favicon);
			}
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			// TODO Auto-generated method stub
			super.onReceivedError(view, errorCode, description, failingUrl);
			if (realClient != null) {
				realClient.onReceivedError(view, errorCode, description,
						failingUrl);
			}

		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO Auto-generated method stub
			isRedirected = false;
			if (url.startsWith("bridgejs:")) {
				isRedirected = true;
				if (instance != null) {
					String[] components = url.split(":");
					String method = components[2];
					String tmpMethod= Uri.decode(method);
					if(tmpMethod.contains(":"))
					{
						method=tmpMethod.replace(":", "");
					}
					else
					{
						method=tmpMethod;
					}
					ArrayList<Object> arrArg = new ArrayList<Object>();
					if (components.length > 3) {
						String argsAsString = components[3];
						String[] formattedArgs = Uri.decode(argsAsString)
								.split(":");
						for (int i = 0; i < formattedArgs.length; i += 2) {
							String type = formattedArgs[i];
							String argStr = formattedArgs[i + 1];
							if (type.equals("f")) {
								String funcId = argStr;
								String arg = Uri.decode(funcId);
								JSFunction function = new JSFunction();
								function.setRemoveAfterExecute(false);
								function.setFuncID(arg);
								function.setWebView(view);
								arrArg.add(function);
							} else if (type.equals("s")) {
								String arg = Uri.decode(argStr);
								arrArg.add(arg);
							}
						}
					}
					Method[] meths = instance.getClass().getMethods();
					Method meth = null;
					for (int i = 0; i < meths.length; i++) {
						if (meths[i].getName().equals(method)
								&& (meths[i].getParameterTypes().length == arrArg
										.size())) {
							meth = meths[i];
							break;
						}
					}
					try {
						meth.invoke(instance, arrArg.toArray());
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return false;
				}
			}
			if (realClient != null) {
				return realClient.shouldOverrideUrlLoading(view, url);
			}
			return super.shouldOverrideUrlLoading(view, url);
		}
	}

}
