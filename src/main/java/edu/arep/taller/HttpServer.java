package edu.arep.taller;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
/**
 * The HttpServer class represents a simple HTTP server.
 */
public class HttpServer {
    private static final int PORT = 35000;
    private static final String STATIC_FILES_DIRECTORY = "src/main/resources/";
    private static String name = "";
    private static final String INDEX_PAGE = "/index.html";

    Map<String, HttpService> getHandlers = new HashMap<>();
    private Map<String, HttpService> postHandlers = new HashMap<>();

    /**
     * Registers a GET request handler for the specified path.
     * 
     * @param path The path for which the handler is registered.
     * @param handler The HTTP service handler for the GET request.
     */
    public void get(String path, HttpService handler) {
        getHandlers.put(path, handler);
    }
    /**
     * Registers a POST request handler for the specified path.
     * 
     * @param path The path for which the handler is registered.
     * @param handler The HTTP service handler for the POST request.
     */
    public void post(String path, HttpService handler) {
        postHandlers.put(path, handler);
    }
    /**
     * Starts the HTTP server and listens for incoming requests.
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClientRequest(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Handles incoming client requests and dispatches them to appropriate handlers.
     * 
     * @param clientSocket The client socket for communication.
     */
    private void handleClientRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {
    
            String requestLine = in.readLine();
            if (requestLine != null) {
                String[] requestComponents = requestLine.split(" ");
                String method = requestComponents[0];
                String path = requestComponents[1];
    
                if ("GET".equals(method) && getHandlers.containsKey(path)) {
                    getHandlers.get(path).process(in, out);
                } else if ("POST".equals(method) && postHandlers.containsKey(path)) {
                    if ("/savename".equals(path)) {
                        StringBuilder requestBody = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            requestBody.append(inputLine);
                        }
                        String[] parts = requestBody.toString().split("&");
                        String[] usernamePair = parts[0].split("=");
                        String username = URLDecoder.decode(usernamePair[1], "UTF-8");
                        System.out.println("Username: " + username);
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        out.write("<h1>Username saved successfully!</h1>".getBytes());
                    } else {
                        postHandlers.get(path).process(in, out);
                    }
                } else {
                    serveStaticFile(out, path);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Serves static files based on the provided path.
     * 
     * @param out The output stream for sending the response.
     * @param path The path of the requested file.
     * @throws IOException If an I/O error occurs while serving the file.
     */
    private static void serveStaticFile(OutputStream out, String path) throws IOException {
        File file = new File(STATIC_FILES_DIRECTORY + path);
        if (file.exists() && !file.isDirectory()) {
            try (InputStream fileInputStream = new FileInputStream(file)) {
                out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } else {
            out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            out.write("<h1>404 Not Found</h1>".getBytes());
        }
    }
    /**
     * The main method to start the HTTP server.
     * 
     * @param args The command-line arguments.
     */
    public static void main(String[] args) {
        HttpServer server = new HttpServer();
        server.get("/movies", (in, out) -> {
            try {
                serveStaticFile(out, "/index.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.get("/getname", (in, out) -> {
            try {
                String storedName = name;
                out.write(storedName.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        server.post("/savename", (in, out) -> {
            try {
                StringBuilder requestBody = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    requestBody.append(inputLine);
                }
                String[] parts = requestBody.toString().split("=");
                name = parts[1];
                out.write("HTTP/1.1 302 Found\r\n".getBytes());
                out.write(("Location: " + INDEX_PAGE + "\r\n\r\n").getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        
        server.start();
    }
}