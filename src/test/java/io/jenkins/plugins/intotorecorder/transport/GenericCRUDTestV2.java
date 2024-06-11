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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;

public class GenericCRUDTestV2 {

    private final String keyFilepath = "src/test/resources/keys/somekey.pem";

    private GenericCRUD transport;
    private RSAKey key;
    private URI uri;

    @Before
    public void setUp() throws Exception {
        String port = System.getenv("ETCD_SERVER_PORT");
        this.uri = new URI("http://localhost:" + port);
        this.transport = new GenericCRUD(uri);
        this.key = RSAKey.read(keyFilepath);
    }

    @Test
    public void testSubmit() throws Exception {
        // Prepare test data
        Link link = new Link(null, null, "step", null, null, null);
        link.sign(this.key);

        // Submit link
        this.transport.submit(link);

        // Verify submission by checking if the link exists in the server
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(this.uri);
        StringEntity input = new StringEntity(link.dumpString());
        input.setContentType("application/x-www-form-urlencoded");
        post.setEntity(input);
        
        HttpResponse response = client.execute(post);
        int statusCode = response.getStatusLine().getStatusCode();
        
        // Assuming a successful submission would return status code 200
        assertTrue(statusCode == 200);
    }
}
