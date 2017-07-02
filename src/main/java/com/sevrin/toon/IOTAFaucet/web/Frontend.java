package com.sevrin.toon.IOTAFaucet.web;

import com.google.gson.Gson;
import com.sevrin.toon.IOTAFaucet.User;
import com.sevrin.toon.IOTAFaucet.backend.Backend;
import com.sevrin.toon.IOTAFaucet.backend.DoWorkRes;
import com.sevrin.toon.IOTAFaucet.backend.HandleRewardResponse;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import io.reactivex.subscribers.SerializedSubscriber;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import spark.ModelAndView;
import spark.Spark;
import spark.template.velocity.VelocityTemplateEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static spark.Spark.port;
import static spark.Spark.staticFiles;
import static spark.Spark.webSocket;

/**
 * Created by toonsev on 6/10/2017.
 */
public class Frontend {
    private static final Gson GSON = new Gson();

    public static void setup(Backend faucet) {
        int port = System.getenv().containsKey("PORT") ? Integer.valueOf(System.getenv("PORT")) : 80;
        port(port);
        staticFiles.location("/public");
        webSocket("/workStream", new WorkWebsocket(faucet));
        Spark.get("/", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("version", Backend.VERSION);
            //TODO: Implement the following

            return new VelocityTemplateEngine().render(new ModelAndView(model, "page.vm"));
        });

        Spark.post("/confirm", (request, response) -> {//the browser sends a confirm
            String message = "Nothing to say";
            String reCaptcha = request.headers("g-recaptcha-response");
            System.out.println(reCaptcha);
            String walletAddress = request.queryParams("walletAddress");
            if (walletAddress == null) {
                message = "no walletAddress :(";
            } else {
                HandleRewardResponse res = faucet.handleReward(new User(walletAddress, request.ip()));
                message = res.getMessage();
            }
            //TODO: Implement the following
            Map<String, Object> model = new HashMap<>();
            model.put("version", Backend.VERSION);
            model.put("message", message);
            return new VelocityTemplateEngine().render(new ModelAndView(model, "confirm.vm"));
        });

        Spark.post("/sendTransaction", (request, response) -> {//the browser sends the PoWed transactionTrytes to the server
            String trytes = request.queryParams("trytes");
            //TODO: Implement the following
            Map<String, Object> model = new HashMap<>();
            return new VelocityTemplateEngine().render(new ModelAndView(model, "sendTransaction.vm"));
        });
    }

    @WebSocket
    public static class WorkWebsocket {
        private Subject<DoWorkRes> workResponses = PublishSubject.<DoWorkRes>create().toSerialized();

        public WorkWebsocket(Backend backend) {
            backend.getWorkObservable(workResponses).subscribe((req) -> {
                String msg = GSON.toJson(req);
                sessions.parallelStream().forEach(session -> {
                    try {
                        session.getRemote().sendString(msg);
                        System.out.println("Send msg to session");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            });

        }

        //TODO: Very important! Pong mechanism to make sure the browser is still active, remove inactive browsers from sessions
        // Store sessions if you want to, for example, broadcast a message to all users
        private Queue<Session> sessions = new ConcurrentLinkedQueue<>();

        @OnWebSocketConnect
        public void connected(Session session) {
            System.out.println("Received a ws connection: " + session + " on ws: " + this.toString());
            sessions.add(session);
        }

        @OnWebSocketClose
        public void closed(Session session, int statusCode, String reason) {
            sessions.remove(session);
        }


        @OnWebSocketMessage
        public void message(Session session, String message) throws IOException {
            try {
                DoWorkRes doWorkRes = GSON.fromJson(message, DoWorkRes.class);
                if (doWorkRes.getHash() == null || doWorkRes.getProcessorId() == null || doWorkRes.getProcessorTransactionUniqueId() == null) {
                    System.out.println("Received doWorkRes without hash, processorId or txUuuid");
                    return;
                }
                workResponses.onNext(doWorkRes);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            System.out.println("Got: " + message);   // Print message
        }

    }
}
