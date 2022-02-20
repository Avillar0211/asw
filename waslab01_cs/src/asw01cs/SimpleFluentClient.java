package asw01cs;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
//This code uses the Fluent API


public class SimpleFluentClient {

	private static String URI = "http://localhost:8080/waslab01_ss/";

	public final static void main(String[] args) throws Exception {
		
    	/* Insert code for Task #4 here */ //Fa un INSERT
		
		/*
		 * System.out.println(Request.Post(URI)
			.bodyForm(Form.form().add("author", "hector").add("tweet_text", "no me gustan los trenes").build())
			.addHeader("Accept", "text/plain").execute().returnContent());
		 */
		
		//Fa print de tots els tweets de la base de dades
		
    	//System.out.println(Request.Get(URI).addHeader("Accept", "text/plain").execute().returnContent());
    	
    	/* Insert code for Task #5 here */ //Elimina un tweet

		Request.Post(URI)
    		.bodyForm(Form.form().add("twid", "101").build())
    		.addHeader("Accept", "delete").execute().returnContent();
    	
    	
    	//Fa print de tots els tweets de la base de dades
    	
    	System.out.println(Request.Get(URI).addHeader("Accept", "text/plain").execute().returnContent());
    	
    }
}

