package edu.arep.taller;
import java.io.*;

public interface HttpService {
    void process(BufferedReader in, OutputStream out) throws IOException;
}
