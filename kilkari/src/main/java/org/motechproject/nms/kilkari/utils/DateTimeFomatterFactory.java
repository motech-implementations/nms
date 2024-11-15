package org.motechproject.nms.kilkari.utils;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.DateTimeParser;

public class DateTimeFomatterFactory {

    public static DateTimeFormatter createFormatter() {
        DateTimeParser[] parsers = {
                DateTimeFormat.forPattern("dd-MM-yyyy").getParser(),
                DateTimeFormat.forPattern("dd/MM/yyyy").getParser(),
                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss").getParser(),
                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS").getParser(),
                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ").getParser(),
                DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ").getParser()
        };
            return new DateTimeFormatterBuilder().append(null, parsers).toFormatter();
        }
    }
