package io.jettra.example.ui;

import io.jettra.ui.component.Badge;
import io.jettra.ui.component.Card;
import io.jettra.ui.component.Div;
import io.jettra.ui.component.Label;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/api/monitor/htmx")
public class MonitoringHtmxResource {

    @GET
    @Path("/nodes")
    @Produces(MediaType.TEXT_HTML)
    public String getNodes() {
        // In a real app, this would fetch from PD
        // For the example, we generate random/static data using Java components
        StringBuilder sb = new StringBuilder();
        
        List<NodeInfo> nodes = new ArrayList<>();
        nodes.add(new NodeInfo("Node-001", "ONLINE", "10.0.0.1:9000", "12%"));
        nodes.add(new NodeInfo("Node-002", "ONLINE", "10.0.0.2:9000", "25%"));
        nodes.add(new NodeInfo("Node-003", "ONLINE", "10.0.0.3:9000", "5%"));
        
        for (NodeInfo node : nodes) {
            Card card = new Card(node.id);
            card.setTitle(node.id);
            
            Badge status = new Badge(node.id + "-status", node.status);
            status.setColor("green");
            card.addComponent(status);
            
            Div details = new Div(node.id + "-details");
            details.setStyleClass("mt-2 text-sm text-gray-500");
            details.addComponent(new Label(node.id + "-ip", "IP: " + node.ip));
            details.addComponent(new Label(node.id + "-cpu", "CPU: " + node.cpu));
            
            card.addComponent(details);
            sb.append(card.render());
        }
        
        return sb.toString();
    }

    private static record NodeInfo(String id, String status, String ip, String cpu) {}
}
