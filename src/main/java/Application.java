import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.DefaultXMLParser;
import ca.uhn.hl7v2.parser.XMLParser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;

public class Application extends RouteBuilder {
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
                        exchange.getIn().setBody(xmlPayload);
                    }
                })
                .to("atlasmap:fhir-mapping.adm")
                .convertBodyTo(String.class)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println(exchange.getIn().getBody(String.class));
                    }
                });
    }

    public static void main(String args[]) throws Exception {
        // POC to use AtlasMap as the transformer
        Main camelMain = new Main();
        camelMain.configure().addRoutesBuilder(new Application());
        camelMain.run(args);
    }
}
