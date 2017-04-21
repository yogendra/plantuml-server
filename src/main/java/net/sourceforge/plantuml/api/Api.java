package net.sourceforge.plantuml.api;

import spark.Request;
import spark.Response;
import spark.utils.IOUtils;
import spark.utils.ResourceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static spark.Spark.*;

/**
 * Created by yogendra on 20/4/17.
 */
public class Api {
    public static void main(String[] args) {
        Api api = new Api();
        api.start();

    }

    private void start() {
        port(8080);

        staticFiles.location("/META-INF/resources");

        redirect.get("/","/ui/index.html");
        redirect.get("/ui", "/ui/index.html");
        get("/ui/*", this::handleUIRequest);


    }

    private Object handleUIRequest(Request request, Response response) {
        String path = request.pathInfo()
                .replace("/ui/", "")
                .split("\\?", 2)[0];

        return staticFile(path, request, response);

    }

    private Object staticFile(String location, Request request, Response response) {
        String prefix = "classpath:public/";
        try {
            String resourceLocation =  prefix + location;
            URL resourceUri = ResourceUtils.getURL(resourceLocation);
            InputStream resource = resourceUri.openConnection().getInputStream();
            String content = IOUtils.toString(resource);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public Object redirectToIndex(Request req, Response res){
        res.redirect("/index.html");
        return null;
    }
}
