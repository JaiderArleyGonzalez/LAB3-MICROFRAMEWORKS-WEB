package edu.arep.taller;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;

public class HttpServerTest {

    private HttpServer server;

    @Before
    public void setUp() {
        server = new HttpServer();
    }

    @Test
    public void testGetMoviesHandler() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        server.get("/movies", (in, out) -> {
            try {
                out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                out.write("<h1>Mock Movies Page</h1>".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        try {
            server.getHandlers.get("/movies").process(new BufferedReader(new StringReader("")), outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String expectedResponse = "HTTP/1.1 200 OK\r\n\r\n<h1>Mock Movies Page</h1>";
        assertEquals(expectedResponse, outputStream.toString());
    }
}