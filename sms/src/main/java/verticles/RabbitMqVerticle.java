package verticles;
import com.rabbitmq.client.*;

import common.MicroServiceVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import services.Services;

import java.io.IOException;

public class RabbitMqVerticle extends MicroServiceVerticle {
	
	 public void start(Future<Void> future) throws Exception {
	        super.start();
	        ConnectionFactory factory = new ConnectionFactory();
	        factory.setHost("localhost");
	        Connection connection = factory.newConnection();
	        Channel channel = connection.createChannel();
	        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

	        Consumer consumer = new DefaultConsumer(channel) {
	          @Override
	          public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
	              throws IOException {
	            JsonObject data = new JsonObject(new String(body));
	            System.out.println(" [x] Received '" + data + "'");
	            if(data.containsKey("emailFlag")) 
	            	Services.Email(data);
	            else if(data.containsKey("smsFlag"))
	            	Services.sendSms(data);
	          }
	        };
	        channel.basicConsume(QUEUE_NAME, true, consumer);  
	    }
	    
	    
  private final static String QUEUE_NAME = "hello";

}