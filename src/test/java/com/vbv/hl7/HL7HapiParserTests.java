package com.vbv.hl7;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v28.message.ADT_A01;
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.parser.XMLParser;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class HL7HapiParserTests {

    @Rule
    SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Test
    public void testXmlParsing() throws IOException {

        HapiContext context = new DefaultHapiContext();
        CanonicalModelClassFactory mcf = new CanonicalModelClassFactory("2.8");
        context.setModelClassFactory(mcf);
        context.getParserConfiguration().setValidating(false);
        InputStream inputStream = HL7HapiParserTests.class.getResourceAsStream("/hl7/complexMessage28.hl7");
        String hl7message = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        try {
            PipeParser parser = context.getPipeParser();
            Message message = parser.parse(hl7message);

            ADT_A01 typedMessage = (ADT_A01) message;
            XMLParser xmlParser = context.getXMLParser();
            String xmlPayload = xmlParser.encode(typedMessage);
            System.out.println(xmlPayload);
        } catch (HL7Exception e) {
            System.out.println("Exception " + e.getLocalizedMessage());
            throw new RuntimeException(e);
        }

    }
}
