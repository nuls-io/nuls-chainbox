package io.nuls.controller;

import io.nuls.core.core.annotation.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-25 11:21
 * @Description: 功能描述
 */
@Path("/")
@Component
public class FrontEndController
{

    @GET
    @Path("www")
    public Response getHtml() throws URISyntaxException {
        return Response.ok(new File("www/index.html")).build();
    }

    @GET
    public void toIndex(@Context  HttpServletRequest req,@Context HttpServletResponse res) throws IOException {
        res.sendRedirect("/www");
    }


}