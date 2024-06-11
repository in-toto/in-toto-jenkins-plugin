package io.jenkins.plugins.intotorecorder.transport;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

import io.github.intoto.legacy.keys.RSAKey;
import io.github.intoto.legacy.models.Link;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

public class GrafeasTransportTestIT {

    private final String keyFilepath = "src/test/resources/keys/somekey.pem";

    private GrafeasTransport transport;
    private RSAKey key;
    private URI uri;

    @Before
    public void setUp() throws Exception {
        String grafeasServerUrl = System.getenv("GRAFEAS_SERVER_URL");
        String noteName = "projects/my-project/notes/my-note";
        String resourceUri = "https://example.com/resource-uri";
        this.uri = new URI(grafeasServerUrl + "?noteName=" + noteName + "&resourceUri=" + resourceUri);
        this.transport = new GrafeasTransport(uri);
        this.key = RSAKey.read(keyFilepath);
    }

    @Test
    public void testGrafeasStorage() throws Exception {

        Link link = new Link(null, null, "step", null, null, null);
        link.sign(this.key);

        /* submit the link to the Grafeas server */
        this.transport.submit(link);

        /* To make sure we got the right thing, we'll get the occurrence
         * information as a string and compare it with our local copy.
         * Modulo any encoding weirdness, they should match exactly as we sent it 
         */
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(this.uri.toString().split("\\?")[0] + "/occurrences");

        ResponseHandler<Response> handler = new ResponseHandler<Response>() {
            @Override
            public Response handleResponse(final HttpResponse response) throws IOException {
                if (response.getStatusLine().getStatusCode() >= 300) {
                    throw new IOException("Server responded with failing status code");
                }
                if (response.getEntity() == null) {
                    throw new IOException("Server's response was invalid");
                }

                Gson gson = new GsonBuilder().create();
                Reader r = new InputStreamReader(response.getEntity().getContent());
                return gson.fromJson(r, Response.class);
            }
        };

        Response response = client.execute(get, handler);
        assertTrue(response.success);
    }

    class Response {
        boolean success;
        String error;
    }
}
