package org.motechproject.nms.kilkari.domain;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

public class JodaTimeModule extends SimpleModule {
    public JodaTimeModule() {
        addSerializer(DateTime.class, new DateTimeSerializer());
        addDeserializer(DateTime.class, new DateTimeDeserializer());
    }

    static class DateTimeSerializer extends JsonSerializer<DateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss");

        @Override
        public void serialize(DateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(formatter.print(value));
        }
    }

    public static class DateTimeDeserializer extends JsonDeserializer<DateTime> {
        @Override
        public DateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String dateString = p.getValueAsString();
            return DateTime.parse(dateString);
        }
    }
}