package io.nuls.controller;

import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @Author: zhoulijun
 * @Time: 2019-07-01 20:07
 * @Description: 功能描述
 */
@Path("/config")
@Component
public class GetRpcConfigController {

    @Path("")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String,Object>> getConfig() throws IOException {
        List<String> list = new ArrayList<>();
        List<Map<String,Object>> res = new ArrayList<>();
        try(BufferedReader listReader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("rpc-config/list")))){
            String line = listReader.readLine();
            while(line != null){
                Log.debug("==>{}",line);
                Map<String,Object> map = new HashMap<>();
                map.put("name",line.replace(".json",""));
                StringWriter stringWriter = new StringWriter();
                try(BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("rpc-config/" + line)));
                    BufferedWriter writer = new BufferedWriter(stringWriter)){
                    reader.transferTo(writer);
                    writer.flush();
                    map.put("config",JSONUtils.json2list(stringWriter.toString(),Map.class));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                res.add(map);
                line = listReader.readLine();
            }

        }

        return res;
    }

    @GET
    @Path("/test")
    public String test(){
        return "hello ";
    }

}
