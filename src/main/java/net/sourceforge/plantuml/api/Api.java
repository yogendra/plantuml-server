package net.sourceforge.plantuml.api;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.code.TranscoderUtil;
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
@Slf4j
public class Api {
    private int port = 3000;
    private String staticFilesLocation = "/META-INF/resources";
    private String uiRoot = "/ui/index.html";
    private String apiRoot = String.format("%s://%s:%s%s", "http", "localhost", "3000", "/");


    public static void main(String[] args) {
        log.info("Starting Api Server");
        Api api = new Api();
        api.start();
        log.info("Started Api Server");
    }


    private void start() {
        log.info("Port: {}", port);
        port(port);

        log.info("Static Files: {}", staticFilesLocation);
        staticFiles.location(staticFilesLocation);
        String defaultRoot = imageUrl("uml", "SyfFKj2rKt3CoKnELR1Io4ZDoSa70000");
        redirect.get("/", defaultRoot);
        redirect.get("/uml", defaultRoot);

        post("/form", this::handleForm);
        post("/", this::handleForm);

        get("/uml/*", this::getUML);
        get("/png/*", this::getPNG);
        get("/svg/*", this::getSVG);
        get("/txt/*", this::getTXT);
    }

    private Object handleForm(Request request, Response response) {
        log.info("Received form request");
        String text = request.queryParams("text");
        try {
            String encoded = TranscoderUtil.getDefaultTranscoder().encode(text);
            String umlUrl = imageUrl("uml", encoded);
            log.debug("text=[{}], encoded=[{}], umlUrl=[{}]", text, encoded, umlUrl);
            response.redirect(umlUrl);
        } catch (IOException e) {
            response.status(500);
            response.body(e.getMessage());
        }
        return null;

    }


    private Object getTXT(Request request, Response response) {
        String encoded = extractEncodedPart(request.pathInfo());
        log.info("Received Text Request: [{}]", encoded);

        return null;
    }

    private Object getSVG(Request request, Response response) {
        String encoded = extractEncodedPart(request.pathInfo());
        log.info("Received SVG Request: [{}]", encoded);
        return null;
    }

    private Object getPNG(Request request, Response response) {
        String encoded = extractEncodedPart(request.pathInfo());
        log.info("Received PNG Request: [{}]", encoded);
        return null;
    }

    private Object getUML(Request request, Response response) {
        try {
            String encoded = extractEncodedPart(request.pathInfo());
            String decoded = TranscoderUtil.getDefaultTranscoder().decode(encoded);
            String textUrl = imageUrl("txt", encoded);
            String pngUrl = imageUrl("png", encoded);
            String umlUrl = imageUrl("uml", encoded);
            String svgUrl = imageUrl("svg", encoded);

            PlantUML uml = new PlantUML(textUrl, svgUrl, pngUrl, umlUrl, decoded);
            log.debug("UML: {}", uml);
            return uml;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private String imageUrl(String type, String encoded) {
        return String.format("%s%s/%s", apiRoot, type, encoded);
    }

    private String extractEncodedPart(String path) {
        return path.replaceAll(".*(uml|png|txt|svg)/", "");
    }

    private Object handleUIRequest(Request request, Response response) {

        String path = request.pathInfo()
                .replace("/ui/", "")
                .split("\\?", 2)[0];
        log.info("Received UI Request for Path: {}", path);
        return staticFile(path, request, response);

    }

    private Object staticFile(String location, Request request, Response response) {
        String prefix = "classpath:public/";
        try {
            String resourceLocation = prefix + location;
            URL resourceUri = ResourceUtils.getURL(resourceLocation);
            InputStream resource = resourceUri.openConnection().getInputStream();
            String content = IOUtils.toString(resource);
            log.info("Service location: {} with content (length:{})", location, content.length());
            return content;
        } catch (IOException e) {
            response.status(500);
            response.body(e.getMessage());
            log.error("Error in serving location: " + location, e);
            return "";
        }
    }
}
