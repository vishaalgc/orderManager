package services;

import java.io.UnsupportedEncodingException;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import constants.Constants;
import io.vertx.core.json.JsonObject;

public class Connector {

	public static JsonObject retrieve(String conditions)
			throws UnirestException, UnsupportedEncodingException {
		JsonObject jObject = null;
		String urlPath = Constants.COUCHDB_END_POINT
				+ Constants.INVOICE_DATABASE + conditions;
		System.out.println("Requesting - " + urlPath);
		HttpResponse<String> response = Unirest.get(urlPath).asString();
		jObject = new JsonObject(response.getBody().toString());
		return jObject;
	}

	public static String postData(String conditions, JsonObject body)
			throws UnirestException {
		String resp = null;
		String urlPath = Constants.COUCHDB_END_POINT
				+ Constants.INVOICE_DATABASE
				+ (conditions != null ? conditions : "");
		System.out.println("POSTING TO - " + urlPath);
		HttpResponse<String> response = Unirest.post(urlPath)
				.header("Content-Type", "application/json")
				.header("accept", "application/json").body(body.toString())
				.asString();
		resp = response.getBody().toString();
		return resp;
	}
}
