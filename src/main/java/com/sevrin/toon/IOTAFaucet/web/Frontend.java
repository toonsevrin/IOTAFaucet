package com.sevrin.toon.IOTAFaucet.web;

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
    public static void setup(Backend faucet) {
        int port = System.getenv().containsKey("port") ? Integer.valueOf(System.getenv("port")) : 80;
        port(port);
        Spark.get("/", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("version", Backend.VERSION);
            //TODO: Implement the following
            return new VelocityTemplateEngine().render(new ModelAndView(model, "page.vm"));
        });

        Spark.post("/confirm", (request, response) -> {//the browser sends a confirm
            String reCaptcha = request.headers("g-recaptcha-response");
            System.out.println(reCaptcha);
            String walletAddress = request.queryParams("walletAddress");
            //TODO: Implement the following
            Map<String, Object> model = new HashMap<>();
            model.put("version", Backend.VERSION);
            return new VelocityTemplateEngine().render(new ModelAndView(model, "confirm.vm"));
        });

        Spark.post("/sendTransaction", (request, response) -> {//the browser sends the PoWed transactionTrytes to the server
            String trytes = request.queryParams("trytes");
            //TODO: Implement the following
            Map<String, Object> model = new HashMap<>();
            return new VelocityTemplateEngine().render(new ModelAndView(model, "sendTransaction.vm"));
        });
    }
}
