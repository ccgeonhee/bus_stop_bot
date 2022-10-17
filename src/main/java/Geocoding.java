import com.slack.api.bolt.App;
import com.slack.api.bolt.jetty.SlackAppServer;

public class Geocoding {
    public static void main(String[] args) throws Exception {
        App app = new App();

        app.command("/bus", (req, ctx) -> ctx.ack(busData.busStop(204000060)));
        app.command("/bus2", (req, ctx) -> ctx.ack(busData.busStop(204000064)));

        SlackAppServer server = new SlackAppServer(app);
        server.start(); // http://localhost:3000/slack/events
    }
}