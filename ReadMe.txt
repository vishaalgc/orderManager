------------------------ READ ME-----------------------

The whole project has been developed in JAVA vertx framework.

This project has three microservices whose names are mentioned below
1. project  (Order microservice)
2. sms (Communication microservice - Includes APIs for Email and Sms)
3. invoice (invoice microservice)

Queuing methodology used : Rabbit Mq (Simple process queuing architecture has been implemented)

Database used : CouchDB (NoSql)

LIMITATIONS / Bounds:

1. Every connectivity has been used via localhost as in (Couch server, RabbitMq server, all three microservices have been tested in same system, but simple change in constants files of respective projects can help connect to different Physical or Internal IPs as need be)
2. Not storing the attachment in any particular storage location like FileSystem or cloud
3. Have integrated the "we will send invoice soon" in direct content of email instead of footer as im not using any templates. Im sure it can be done, ill update here if i find a fix.
4. SMS API isnt very active. I tried various ones from mashape but none of them is stable. The one i integrated currently works fine but
doesnt deliver properly at times. Code can be verified for the same.


ARCHITECTURE FLOW:

ORDER-MS -> COMMUNICATION-MS -> INVOICE-MS

On succesful placing of order we call Communication API which includes both sending email and sms where in internally INVOICE-MS is called to generate a invoice and depending on success of failure of invoice generation email context is changes and handled accordingly.
Attachments are working fine.

--------------------------------------------------------
Sample ORDER Database record:
{
  "_id": "9cecf46711d9e6aca1944cdbb3010ff3",
  "_rev": "1-6c03afda64908c4f64ded3d9798ada2a",
  "product_id": "32345",
  "firstname": "vishaal",
  "email": "vishaalgc@gmail.com",
  "phone": "7415169372",
  "quantity": 1,
  "price": 5000,
  "product_name": "Adidas shoes",
  "createdAt": "2017/11/12 22:59:28"
}
-------------------------------------------------------

Sample INVOICES Database record:
{
  "_id": "9cecf46711d9e6aca1944cdbb3011d51",
  "_rev": "1-f4e22115c7928d83d1d7dcb4e908ac02",
  "productName": "Adidas shoes",
  "billedTo": "vishaal",
  "quantity": "1",
  "order_id": "9cecf46711d9e6aca1944cdbb3010ff3",
  "price": "5000",
  "invoiceStatus": "Successful",
  "createdAt": "2017/11/12 22:59:34"
}

--------------------------------------------------------


Sample Request for Create Order:

curl -X POST \
  http://localhost:8082/createOrder \
  -H 'cache-control: no-cache' \
  -H 'postman-token: c5092d6f-9d5f-b144-cef3-ebd0d3498ce5' \
  -d '{
	"data": {
		"product_id": "32345",
		"firstname": "vishaal",
		"email" : "vishaalgc@gmail.com",
		"phone" : "7415169372",
		"quantity" : 1,
		"price" : 5000,
		"product_name" : "Adidas shoes"
	}
}'

----------------------------------------------------------

Sample Response from Create Order

{
    "data": "order created succesfully",
    "Message": {
        "order_id": "4728475e8f7f114f9bef3773b4000e7c"
    },
    "success": 1
}

---------------------------------------------------------

Additional items to enhance the same project:

Product Microservice to Validate a given products availability and price before placing order
HTML Mail templates storage in database and fetch different ones run time dynamically


