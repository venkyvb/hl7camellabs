package com.vbv.hl7;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v23.message.ADT_A08;
import ca.uhn.hl7v2.model.v23.segment.PID;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.XMLParser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.FhirPublication;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.jupiter.api.Test;

public class HL7SimpleRouteTests extends CamelTestSupport {

    @Rule
    SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    @Override
    protected RoutesBuilder createRouteBuilder() {

        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file:src/test/resources/hl7?noop=true")
                        .convertBodyTo(String.class)
                        .unmarshal()
                        .hl7(false)
                        .log("The Message body is: ${body}")
                        .process(new Processor() {
                            public void process(Exchange exchange) throws Exception {
                                final Message message = exchange.getIn().getBody(Message.class);
                                XMLParser xmlParser = new DefaultXMLParser();
                                String xmlPayload = xmlParser.encode(message);
                                System.out.println("XML version: " + xmlPayload);
                            }
                        })
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                final ADT_A08 msg = exchange.getIn().getBody(ADT_A08.class);
                                final PID pid = msg.getPID();
                                String surname = pid.getPatientName(0).getFamilyName().getValue();
                                String givenName = pid.getPatientName(0).getGivenName().getValue();
                                String patientId = pid.getPid3_PatientIDInternalID(0).getCx1_ID().getValue();
                                Patient patient = new Patient();
                                patient.addName().addGiven(givenName);
                                patient.getNameFirstRep().setFamily(surname);
                                patient.setId(patientId);
                                exchange.getIn().setBody(patient);
                            }
                        })
                        .marshal().fhirJson("R4")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                System.out.println("FHIR JSON: " + exchange.getIn().getBody(String.class));
                            }
                        })
                        .convertBodyTo(String.class)
                        .to("mock:result");
            }
        };
    }

    @Test
    public void executeTest() throws InterruptedException {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedBodyReceived().body(String.class);

        assertMockEndpointsSatisfied();

    }
}

