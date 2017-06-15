package com.sevrin.toon.IOTAFaucet.web;

import com.sevrin.toon.IOTAFaucet.IOTAFaucet;
import spark.ModelAndView;
import spark.Spark;
import spark.template.velocity.VelocityTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.port;

/**
 * Created by toonsev on 6/10/2017.
 */
public class Frontend {
    public static void setup(IOTAFaucet faucet) {
        int port = System.getenv().containsKey("port") ? Integer.valueOf(System.getenv("port")) : 80;
        port(port);
        Spark.get("/", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("version", IOTAFaucet.VERSION);
            return new VelocityTemplateEngine().render(new ModelAndView(model, "page.vm"));
        });

        Spark.post("/confirm", (request, response) -> {
            String res = request.headers("g-recaptcha-response");
            System.out.println(res);
            Map<String, Object> model = new HashMap<>();
            model.put("version", IOTAFaucet.VERSION);
            return new VelocityTemplateEngine().render(new ModelAndView(model, "confirm.vm"));
        });
    }
}
