package com.dotchi1.backend.json;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

public class BooleanDeserializer extends JsonDeserializer<Boolean> {

	@Override
	public Boolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		String value = jsonParser.getText();
		if (value.equals("1"))
			return true;
		else
			return false;
	}

}
