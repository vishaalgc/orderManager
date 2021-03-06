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

public class SmsVerticle extends MicroServiceVerticle {
	 private int port = 8083;

	    @Override
	    public void start(Future<Void> future) throws Exception {
	        super.start();

	        Future<HttpServer> httpEndPointReady = configureTheHTTPServer();

	        // Publish on dashboard
	        publishHttpEndpoint("order", "localhost", config().getInteger("http.port", port), ar -> {
	            if (ar.failed()) {
	                ar.cause().printStackTrace();
	            } else {
	                System.out.println("Communication Microservices (Api) has been published: on port " + port + " " + ar.succeeded());
	            }
	        });
	    }
	    
	    private Future<HttpServer> configureTheHTTPServer() {
	        Future<HttpServer> httpServerFuture = Future.future();

	        // Use a Vert.x web router for this REST API
	        Router router = Router.router(vertx);

	        // Com APIs
	        router.post("/sendSms").handler(this::sendSms);
	        router.post("/sendEmail").handler(this::sendEmail);
	        
	        // CORS OPTIONS
	        router.options("/sendSms").handler(this::sendSms);
	        router.options("/sendEmail").handler(this::sendEmail);
	        
	        vertx.createHttpServer().requestHandler(router::accept).listen(port, httpServerFuture.completer());
	        return httpServerFuture;
	    }
	    
	    public void sendSms(RoutingContext routingContext) {
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
	                        String name = dataObject.getValue("product_name") != null ? dataObject.getValue("product_name").toString() : null ;
	                        String price = dataObject.getValue("price")!= null ? dataObject.getValue("price").toString() : null;
	                        String order_id = dataObject.getValue("order_id")!= null ? dataObject.getValue("order_id").toString() : null;
	                        String phone =  dataObject.getValue("phone")!= null ? dataObject.getValue("phone").toString() : null;
	                        if (checkNullOrEmptyInt(routingContext, name, "product_name")) {
	                            future.fail("-1");
	                            return;
	                        } else if (checkNullOrEmptyInt(routingContext, phone, "phone")) {
	                            future.fail("-1");
	                            return;
	                        } else if (checkNullOrEmptyInt(routingContext, price, "price")) {
	                            future.fail("-1");
	                            return;
	                        } 
	                        else if (checkNullOrEmptyInt(routingContext, order_id, "order_id")) {
	                            future.fail("-1");
	                            return;
	                        } 
	                        dataObject.put("smsFlag", true);
	                        future.complete(Services.RabbitMqPush(dataObject));
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
	 	                            resultMap.put("data", "Sms triggered succesfully");
	 	                            resultMap.put("Message", asyncResult.result());
	                            }
	                            else {
		                            resultMap.put("success", 0);
		                            resultMap.put("data", null);
		                            resultMap.put("Message", "There was an error while generating sms");
	                            }
	                        }
	                        else
	                        {
	                        	if (asyncResult.result() != null) {
	                            	resultMap.put("success", 1);
	 	                            resultMap.put("data", "Sms triggered succesfully");
	 	                            resultMap.put("Message", asyncResult.result());
	                            }
	                            else {
		                            resultMap.put("success", 0);
		                            resultMap.put("data", null);
		                            resultMap.put("Message", "There was an error while generating sms");
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

	    public void sendEmail(RoutingContext routingContext) {
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
	                        String toEmail = dataObject.getValue("toEmail") != null ? dataObject.getValue("toEmail").toString() : null ;
	                        String customerName = dataObject.getValue("customerName") != null ? dataObject.getValue("customerName").toString() : null ;
	                        String fromEmail = dataObject.getValue("fromEmail")!= null ? dataObject.getValue("fromEmail").toString() : null;
	                        String emailSubject = dataObject.getValue("emailSubject")!= null ? dataObject.getValue("emailSubject").toString() : null;
	                        String emailContent = dataObject.getValue("emailContent")!= null ? dataObject.getValue("emailContent").toString() : null;
	                        String attachments =  dataObject.getValue("attachments")!= null ? dataObject.getValue("attachments").toString() : null;
	                        String order_id =  dataObject.getValue("order_id")!= null ? dataObject.getValue("order_id").toString() : null;
	                        if (checkNullOrEmptyInt(routingContext, toEmail, "toEmail")) {
	                            future.fail("-1");
	                            return;
	                        } else if (checkNullOrEmptyInt(routingContext, fromEmail, "fromEmail")) {
	                            future.fail("-1");
	                            return;
	                        } else if (checkNullOrEmptyInt(routingContext, emailSubject, "emailSubject")) {
	                            future.fail("-1");
	                            return;
	                        } else if (checkNullOrEmptyInt(routingContext, emailContent, "emailContent")) {
	                            future.fail("-1");
	                            return;
	                        } else if (checkNullOrEmptyInt(routingContext, emailContent, "emailContent")) {
	                            future.fail("-1");
	                            return;
	                        } else if (checkNullOrEmptyInt(routingContext, order_id, "order_id")) {
	                            future.fail("-1");
	                            return;
	                        } else if (checkNullOrEmptyInt(routingContext, customerName, "customerName")) {
	                            future.fail("-1");
	                            return;
	                        } 
	                        dataObject.put("emailFlag", true);
	                        future.complete(Services.RabbitMqPush(dataObject));
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
	 	                            resultMap.put("data", "Email trigggerd successfully");
	 	                            resultMap.put("Message", asyncResult.result());
	                            }
	                            else {
		                            resultMap.put("success", 0);
		                            resultMap.put("data", null);
		                            resultMap.put("Message", "There was an error while generating email");
	                            }
	                        }
	                        else
	                        {
	                        	if (asyncResult.result() != null) {
	                            	resultMap.put("success", 1);
	 	                            resultMap.put("data", "Email trigggerd successfully");
	 	                            resultMap.put("Message", asyncResult.result());
	                            }
	                            else {
		                            resultMap.put("success", 0);
		                            resultMap.put("data", null);
		                            resultMap.put("Message", "There was an error while generating email");
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
