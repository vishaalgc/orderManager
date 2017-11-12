package main;

import io.vertx.core.Vertx;
import verticles.InvoiceVerticle;

public class InvoiceEndPoint {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new InvoiceVerticle());
	}
}

