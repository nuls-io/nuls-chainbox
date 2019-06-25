package io.nuls.controller;

import io.nuls.core.core.annotation.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-25 11:21
 * @Description: 功能描述
 */
@Path("/www")
@Component
public class FrontEndController
{

    @GET
    @Path("/{docPath:.*}.{ext}")
    public Response getHtml(@PathParam("docPath") String docPath, @PathParam("ext") String ext) throws URISyntaxException {
        URL url = this.getClass().getClassLoader().getResource("www" + File.separator + cleanDocPath(docPath).replaceAll("/",File.separator) + "." + ext);
        return Response.ok(new File(url.toURI())).build();
    }

    @GET
    @Path("{docPath:.*}")
    public Response getFolder(@PathParam("docPath") String docPath) throws URISyntaxException {
        if ("".equals(docPath) || "/".equals(docPath))
        {
            return getHtml("index","html");
        }
        else
        {
            return getHtml(cleanDocPath(docPath) + "/index","html");
        }
    }

    private String cleanDocPath(String docPath)
    {
        if (docPath.startsWith("/"))
        {
            return docPath.substring(1);
        }
        else
        {
            return docPath;
        }
    }
}