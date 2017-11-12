package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.body.Body;
import com.mashape.unirest.request.body.RequestBodyEntity;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class Services {
	
	public static JsonObject createInvoiceRecord(JsonObject inputJson) {
		if(inputJson == null)
			return null;
		String response = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		inputJson.put("createdAt",dateFormat.format(date));
		try {
			response = Connector.postData(null, inputJson);
		} catch (UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(response == null)
			return null;
		JsonObject resp = new JsonObject(response);
		if(resp.containsKey("ok")) {
			JsonObject output = new JsonObject();
			output.put("invoice_id", resp.getString("id"));
			output.put("order_id", inputJson.getString("order_id"));
			return output;
		}
		return null;
	}
	public static JsonObject generateInvoice(JsonObject data) {
		try {
			JsonObject postObj = new JsonObject();
			postObj.put("from","Meesho");
			postObj.put("to",data.getString("billedTo"));
			postObj.put("logo","https://s3-ap-southeast-1.amazonaws.com/meesho-notifications/meesho-web/landing/meesho_footer_logo.png");
			postObj.put("number","1");
			
			JsonArray items = new JsonArray();
			JsonObject temp=new JsonObject();
			temp.put("name", data.getString("productName"));
			temp.put("quantity", data.getValue("quantity"));
			temp.put("unit_cost", data.getValue("price"));
			items.add(temp);
			postObj.put("items",items);
			postObj.put("notes","Thanks for your business!");
			HttpResponse<String> response = Unirest.post("https://invoice-generator.com/")
					  .header("content-type", "application/json")
					  .body(postObj.toString())
					   .asString();
			InputStream is = response.getRawBody();
			FileOutputStream fos = new FileOutputStream(new File(data.getString("order_id")+".pdf"));
			int inByte;
			while((inByte = is.read()) != -1)
			     fos.write(inByte);
			fos.close();
			is.close();
			File tmpDir = new File(data.getString("order_id")+".pdf");
			boolean exists = tmpDir.exists();

			if(exists) {
				data.put("invoiceStatus","Successful");
				JsonObject output = createInvoiceRecord(data);
				return new JsonObject().put("invoice_data", output);
			}
			else {
				data.put("invoiceStatus","Failed");
				createInvoiceRecord(data);
			}
			
		} catch (IOException | UnirestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new JsonObject().put("Error","There was an error while generating invoice");
	}
}
