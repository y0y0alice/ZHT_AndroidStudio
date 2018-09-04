package com.skyland.zht;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class UserTypeAdapter extends TypeAdapter<UserTypeEnum> {

	public UserTypeAdapter() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public UserTypeEnum read(JsonReader reader) throws IOException {
		// TODO Auto-generated method stub
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}
		String type = reader.nextString();
		if (type.equals("0")) {
			return UserTypeEnum.Login;
		}
		return null;
	}

	@Override
	public void write(JsonWriter writer, UserTypeEnum type) throws IOException {
		// TODO Auto-generated method stub
		if (type == null) {
			writer.nullValue();
			return;
		}
		writer.value(type.getValue());
	}

}
