/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.tailormap.viewer;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 *
 * @author Roy Braam
 * @author mprins
 */
public class HttpTestSupport{
    
    protected HttpServer httpServer;
    
    public HttpTestSupport(){
        try{
            httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
    
    @BeforeEach
    public void setUpHttpServer() {
        httpServer.start();
    }
    
    @AfterEach
    public void tearDownHttpServer() {
        httpServer.stop(0);
    }
}