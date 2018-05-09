package api.controllers;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;

@Path("test")
public class TestController {

    public TestController() {
        super();
    }

    @GET
    public String doGet(@Context Request req) {
        return "Hello world";
    }

}
