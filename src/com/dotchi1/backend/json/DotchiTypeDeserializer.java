package com.dotchi1.backend.json;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.dotchi1.model.BaseActivityItem.ActivityType;

public class DotchiTypeDeserializer extends JsonDeserializer<ActivityType> {

	@Override
	public ActivityType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		
		String valStr = jsonParser.getText();
		ActivityType[] arr = ActivityType.values();
		return arr[Integer.parseInt(valStr)];
	}

}
