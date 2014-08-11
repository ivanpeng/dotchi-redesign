package com.dotchi1.backend.json;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

public class DateDeserializer extends JsonDeserializer<Date> {

	@Override
	public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = jsonParser.getText();
		date = date.replace("/", "-");
		if (date.equals(""))
			return null;
		try	{
			return sdf.parse(date);
		} catch (ParseException e)	{
			throw new RuntimeException(e);
		}
	}

}
