package de.meierj.small_http_server;

import java.io.IOException;

public class App 
{
    public static void main( String[] args ) throws IOException
    {
        SmallHttpServer smallHttpServer = new SmallHttpServer(2222);
        smallHttpServer.addHandler("/", exchange -> exchange.sendResponse("Hello World!"));
    }
}
