package org.motechproject.nms.testing.it.mcts;

import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.nms.mcts.contract.AnmAshaDataSet;
import org.motechproject.nms.mcts.contract.ChildrenDataSet;
import org.motechproject.nms.mcts.contract.MothersDataSet;
import org.motechproject.nms.mcts.service.MctsWebServiceFacade;
import org.motechproject.testing.osgi.BasePaxIT;
import org.motechproject.testing.osgi.container.MotechNativeTestContainerFactory;
import org.motechproject.testing.osgi.http.SimpleHttpServer;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import static junit.framework.Assert.assertEquals;


@RunWith(PaxExam.class)
@ExamReactorStrategy(PerSuite.class)
@ExamFactory(MotechNativeTestContainerFactory.class)
public class MctsImportBundleIT extends BasePaxIT {

    @Inject
    private MctsWebServiceFacade mctsWebServiceFacade;

    @Test
    public void shouldDeserializeMothersDataFromSoapResponse() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(this.getClass().getResourceAsStream("/mcts/mcts-mothers-data.xml"), writer);
        String response = writer.toString();

        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("mctsEndpoint", 200, response);

        URL endpoint = new URL(url);
        LocalDate referenceDate = LocalDate.now().minusDays(1);

        //TODO resolve problem with class loader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mctsWebServiceFacade.getClass().getClassLoader());
        MothersDataSet result = mctsWebServiceFacade.getMothersData(referenceDate, referenceDate, endpoint, 21l);
        Thread.currentThread().setContextClassLoader(cl);

        //TODO add assert
    }

    @Test
    public void shouldDeserializeChildrenDataFromSoapResponse() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(this.getClass().getResourceAsStream("/mcts/mcts-children-data.xml"), writer);
        String response = writer.toString();

        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("mctsEndpoint", 200, response);

        URL endpoint = new URL(url);
        LocalDate referenceDate = LocalDate.now().minusDays(1);

        ChildrenDataSet result = mctsWebServiceFacade.getChildrenData(referenceDate, referenceDate, endpoint, 21l);

        //TODO add assert
    }

    @Test
    public void shouldDeserializeAnmAshanDataFromSoapResponse() throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(this.getClass().getResourceAsStream("/mcts/mcts-anm-asha-data.xml"), writer);
        String response = writer.toString();

        SimpleHttpServer simpleServer = SimpleHttpServer.getInstance();
        String url = simpleServer.start("mctsEndpoint", 200, response);

        URL endpoint = new URL(url);
        LocalDate referenceDate = LocalDate.now().minusDays(1);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mctsWebServiceFacade.getClass().getClassLoader());
        AnmAshaDataSet result = mctsWebServiceFacade.getAnmAshaData(referenceDate, referenceDate, endpoint, 21l);
        Thread.currentThread().setContextClassLoader(cl);

        assertEquals(10, result.getRecords().size());
        assertEquals("Rupi Dei", result.getRecords().get(0).getName());
        assertEquals("Mamata Kausalya", result.getRecords().get(1).getName());
        assertEquals("Shantilata Mallik", result.getRecords().get(2).getName());
        assertEquals("Santilata Sandil", result.getRecords().get(3).getName());
        assertEquals("Hiramani Munda", result.getRecords().get(4).getName());
        assertEquals("Nima Mahanta", result.getRecords().get(5).getName());
        assertEquals("Anjali Jana", result.getRecords().get(6).getName());
        assertEquals("Suchitra Barik", result.getRecords().get(7).getName());
        assertEquals("Sobhasini Majhi", result.getRecords().get(8).getName());
        assertEquals("Sima Pradhan", result.getRecords().get(9).getName());
    }
}