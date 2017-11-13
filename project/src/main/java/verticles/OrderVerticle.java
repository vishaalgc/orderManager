package verticles;

import java.util.HashMap;
import java.util.Map;

import common.Common;
import common.MicroServiceVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import services.Services;

public class OrderVerticle extends MicroServiceVerticle {
	 private int port = 8082;

	    @Override
	    public void start(Future<Void> future) throws Exception {
	        super.start();

	        Future<HttpServer> httpEndPointReady = configureTheHTTPServer();

	        // Publish on dashboard
	        publishHttpEndpoint("order", "localhost", config().getInteger("http.port", port), ar -> {
	            if (ar.failed()) {
	                ar.cause().printStackTrace();
	            } else {
	                System.out.println("Order Microservices (Api) has been published: on port " + port + " " + ar.succeeded());
	            }
	        });
	    }
	    
	    private Future<HttpServer> configureTheHTTPServer() {
	        Future<HttpServer> httpServerFuture = Future.future();

	        // Use a Vert.x web router for this REST API
	        Router router = Router.router(vertx);

	        // Order APIs
	        router.post("/createOrder").handler(this::createOrder);
	        
	        // CORS OPTIONS
	        router.options("/createOrder").handler(this::createOrder);
	        
	        vertx.createHttpServer().requestHandler(router::accept).listen(port, httpServerFuture.completer());
	        return httpServerFuture;
	    }
	    
	    public void createOrder(RoutingContext routingContext) {
	        routingContext.request().bodyHandler(new Handler<Buffer>() {
	            @Override
	            public void handle(Buffer event) {
	                vertx.executeBlocking(future -> {
	                    if (!isJSONValid(event.toString())) {
	                        JsonObject temp = new JsonObject();
	                        temp.put("Error", "The given post data is not a valid json format");
	                        future.complete(temp);
	                        return;
	                    }
	                    JsonObject params = new JsonObject(event.toString());
	                    JsonObject dataObject = null;
	                    if (params.containsKey("data")) {
	                    	Object val = params.getValue("data");
	                    	if(val instanceof String){
	                    		dataObject = new JsonObject(val.toString());
	                    	}
	                    	else if(val instanceof JsonObject)
	                    		dataObject = params.getJsonObject("data");
	                    	else {
	                    		JsonObject temp = new JsonObject();
	                    		temp.put("Error", "The given post data is not a valid json format");
	                            future.complete(temp);
	                            return;
	                    	}
	                        if (dataObject == null)
	                            future.complete(null);
	                    } else {
	                        JsonObject temp = new JsonObject();
	                        temp.put("Error", "post data format is not proper");
	                        future.complete(temp);
	                        return;
	                    }
	                    try {
	                        String product_id = dataObject.getValue("product_id") != null ? dataObject.getValue("product_id").toString() : null ;
	                        String firstname = dataObject.getValue("firstname")!= null ? dataObject.getValue("firstname").toString() : null;
	                        String lastname = dataObject.getValue("lastname")!= null ? dataObject.getValue("lastname").toString() : null;
	                        String email = dataObject.getValue("email")!= null ? dataObject.getValue("email").toString() : null;
	                        String phone =  dataObject.getValue("phone")!= null ? dataObject.getValue("phone").toString() : null;
	                        String quantity =  dataObject.getValue("quantity")!= null ? dataObject.getValue("quantity").toString() : null;
	                        String price =  dataObject.getValue("price")!= null ? dataObject.getValue("price").toString() : null;
	                        String product_name =  dataObject.getValue("product_name")!= null ? dataObject.getValue("product_name").toString() : null;
	                        if (checkNullOrEmptyInt(routingContext, product_id, "product_id")) {
	                            future.fail("-1");
	                            return;
	                        } else if (checkNullOrEmptyInt(routingContext, firstname, "firstname")) {
	                            future.fail("-1");
	                            return;
	                        } else if (checkNullOrEmptyInt(routingContext, email, "email")) {
	                            future.fail("-1");
	                            return;
	                        } else if (checkNullOrEmptyInt(routingContext, phone, "phone")) {
	                            future.fail("-1");
	                            return;
	                        } else if (checkNullOrEmptyInt(routingContext, quantity, "quantity")) {
	                            future.fail("-1");
	                            return;
	                        } else if (checkNullOrEmptyInt(routingContext, price, "price")) {
	                            future.fail("-1");
	                            return;
	                        } 
	                        else if (checkNullOrEmptyInt(routingContext, product_name, "product_name")) {
	                            future.fail("-1");
	                            return;
	                        } 

	                        future.complete(Services.createOrder(dataObject));
	                    } catch (Exception e) {
	                        // TODO Auto-generated catch block
	                        e.printStackTrace();
	                    }
	                }, asyncResult -> {
	                    if (asyncResult.succeeded()) {
	                        System.out.println("inside asyncResult success");
	                        Map<String, Object> resultMap = new HashMap<String, Object>();
	                        JsonObject result = new JsonObject();
	                        if (asyncResult.result() instanceof JsonObject) {
	                            result = (JsonObject) asyncResult.result();
	                            System.out.println(result);
	                            if(result != null && result.containsKey("Error")){
	                            	 	resultMap.put("success", 0);
			                            resultMap.put("data", result);
	                            }
	                            else if (asyncResult.result() != null) {
	                            	resultMap.put("success", 1);
	 	                            resultMap.put("data", "order created succesfully");
	 	                            resultMap.put("Message", asyncResult.result());
	                            }
	                            else {
		                            resultMap.put("success", 0);
		                            resultMap.put("data", null);
		                            resultMap.put("Message", "There was an error while creating order");
	                            }
	                        }
	                        else
	                        {
	                        	if (asyncResult.result() != null) {
	                            	resultMap.put("success", 1);
	 	                            resultMap.put("data", "order created succesfully");
	 	                            resultMap.put("Message", asyncResult.result());
	                            }
	                            else {
		                            resultMap.put("success", 0);
		                            resultMap.put("data", null);
		                            resultMap.put("Message", "There was an error while creating order");
	                            }
	                        }
	                        Common.getHeaders(routingContext).setStatusCode(200).end(Json.encodePrettily(resultMap));
	                    }
	                    if (asyncResult.failed() && asyncResult.cause().toString().equalsIgnoreCase("-1")) {
	                        System.out.println("inside result failed, cause: " + asyncResult.cause().toString());
	                        Common.getHeaders(routingContext).setStatusCode(200).end(Json.encodePrettily("Result Failed"));
	                    }
	                });

	            }
	        });

	    }

	    public boolean checkNullOrEmptyInt(RoutingContext c, String s, String key) {

	        Map<String, Object> resultMap = new HashMap<String, Object>();
	        resultMap.put("success", 0);
	        resultMap.put("Message", key + " is required and cannot be null");
	        resultMap.put("data", "null");

	        try {
	            if (s == null || s.equalsIgnoreCase("")) {
	                if (!s.contains(",") && !(Integer.parseInt(s) > 0)) {
	                    Common.getHeaders(c).setStatusCode(200).end(Json.encodePrettily(resultMap));
	                    return true;
	                }
	            }

	        } catch (Exception e) {
	        	Common.getHeaders(c).setStatusCode(200).end(Json.encodePrettily(resultMap));
	            return true;
	        }

	        return false;
	    }

	    public boolean isJSONValid(String test) {
	        try {
	        	System.out.println(test);
	            new JsonObject(test);
	        } catch (Exception ex) {
	            try {
	                new JsonArray(test);
	            } catch (Exception ex1) {
	                return false;
	            }
	        }
	        return true;
	    }

}
