package org.motechproject.nms.rch.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

public final class MarshallUtils {

    private MarshallUtils() {
    }

    public static Object unmarshall(String xml, Class... classesToBeBound) throws JAXBException {

        JAXBContext context = JAXBContext.newInstance(classesToBeBound);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        StringReader reader = new StringReader(xml);

        return unmarshaller.unmarshal(reader);
    }
    public static Object unmarshallJson(String jsonResult, Class... classesToBeBound) throws JAXBException {

        JAXBContext jc = JAXBContext.newInstance(classesToBeBound);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setProperty("eclipselink.media-type", "application/json");
        unmarshaller.setProperty("eclipselink.json.include-root", false);

        StringReader reader = new StringReader(jsonResult);

        return unmarshaller.unmarshal(reader);
    }
}
