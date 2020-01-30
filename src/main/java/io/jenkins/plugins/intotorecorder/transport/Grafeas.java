/**
 *
 */
package io.jenkins.plugins.intotorecorder.transport;

import io.github.in_toto.models.Link;
import java.net.URI;
import java.util.*;
import com.google.gson.Gson;

import java.io.IOException;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;


public class Grafeas extends Transport {

    URI uri;
    GrafeasOccurrence occurrence;

    public class GrafeasOccurrence {
        public String name;
        public String noteName;
        public Map<String, String> resource = new HashMap<String, String>();
        public String kind = "INTOTO";
        public Link intoto;

        public GrafeasOccurrence(String name, String noteName, String resourceUri) {
            this.name = name;
            this.noteName = noteName;
            this.resource.put("uri", resourceUri);
        }
    }

    private static Map<String, String> getParameterMap(String parameterString) {
        String[] items = parameterString.split("&");
        Map<String, String> parameterMap = new HashMap<String, String>();
        for (String item : items) {
            String[] pair = item.split("=");
            parameterMap.put(pair[0], pair[1]);
        }
        return parameterMap;
    }

    public Grafeas(URI uri) {
        this.uri = uri;

        String parameterString = uri.getQuery();

        Map<String, String> parameterMap = this.getParameterMap(parameterString);

        GrafeasOccurrence occurrence = new GrafeasOccurrence(
            parameterMap.get("name"),
            parameterMap.get("noteName"),
            parameterMap.get("resourceUri")
        );

        this.occurrence = occurrence;
    }

    public void submit(Link link) {
        this.occurrence.intoto = link;

        Gson gson = new Gson();
        String jsonString = gson.toJson(this.occurrence);

        String destination = this.uri.toString().split("\\?")[0].substring("grafeas+".length());

        // FIXME: Shamelessly copied from GenericCRUD.java
        try {
            HttpRequest request = new NetHttpTransport()
                .createRequestFactory()
                .buildPostRequest(new GenericUrl(destination),
                    ByteArrayContent.fromString("application/x-www-form-uriencoded",
                        jsonString));
            HttpResponse response = request.execute();
            System.out.println(response.parseAsString());

            /* FIXME: should handle error codes and other situations more appropriately,
             * but this gets the job done for a PoC
             */
        } catch (IOException e) {
            throw new RuntimeException("couldn't serialize to HTTP server: " + e);
        }
    }
}
