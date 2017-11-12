package main;

import io.vertx.core.Vertx;
import verticles.RabbitMqVerticle;
import verticles.SmsVerticle;

public class SmsEndPoint {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new SmsVerticle());
		vertx.deployVerticle(new RabbitMqVerticle());
	}
}

