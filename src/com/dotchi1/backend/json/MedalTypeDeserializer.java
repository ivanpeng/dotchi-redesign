package com.dotchi1.backend.json;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import com.dotchi1.model.VoteItem.MedalType;

public class MedalTypeDeserializer extends JsonDeserializer<MedalType> {

	@Override
	public MedalType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		String valStr = jsonParser.getText();
		MedalType[] arr = MedalType.values();
		for (int i = 0; i < arr.length; i++)	{
			if (arr[i].getValue().equals(valStr))
				return arr[i];
		}
		// If nothing, return none
		return arr[arr.length-1];

	}

}
