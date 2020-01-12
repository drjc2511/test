package biz.smartcommerce.azure.cmdrs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import biz.smartcommerce.azure.cmdrs.controller.CmdrsDBController;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function extends HttpServlet {
    /**
     * This function listens at endpoint "/api/HttpTrigger-Java". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpTrigger-Java&code={your function key}
     * 2. curl "{your host}/api/HttpTrigger-Java?name=HTTP%20Query&code={your function key}"
     * Function Key is not needed when running locally, it is used to invoke function deployed to Azure.
     * More details: https://aka.ms/functions_authorization_keys
     */
	
 	private static final Gson gson = new Gson();
 	
 	@FunctionName("Init")
    public HttpResponseMessage runInit(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
   
        context.getLogger().info("Initializing DB and Solr...");
        String result = StringUtils.EMPTY;
        try {
        	initDBAndSolr();
		} catch (Exception e) {
			e.printStackTrace();
	        return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("ERROR").build();
		}
        
        return request.createResponseBuilder(HttpStatus.OK).body(result).build();
    }
 	
 	@FunctionName("Search")
    public HttpResponseMessage runSearch(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
   
        context.getLogger().info("Searching...");
        String result = StringUtils.EMPTY;
        try {
			result = querySolr(request.getQueryParameters().get("FIELD"), request.getQueryParameters().get("VALUE"));
		} catch (SolrServerException | IOException e) {
			e.printStackTrace();
	        return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("ERROR").build();
		}
        
        return request.createResponseBuilder(HttpStatus.OK).body(result).build();
    }
 	
    @FunctionName("HttpTrigger-Java")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET, HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
   
        context.getLogger().info("Java HTTP trigger processed a request.");
        String result = StringUtils.EMPTY;
        try {
			String dbJson = createDBEntry(request);
			context.getLogger().info(dbJson);
			Thread.sleep(10000);
			result = querySolr(request.getQueryParameters().get("VALUE"));
			
		} catch (SolrServerException | IOException | InterruptedException e) {
			e.printStackTrace();
	        return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("ERROR").build();
		}
        
        return request.createResponseBuilder(HttpStatus.OK).body(result).build();
    }
    
    private String querySolr(String value) throws SolrServerException, IOException {
    	return querySolr("Value",value);
    }
    
    private String querySolr(String field, String value) throws SolrServerException, IOException {
    	final String solrUrl = "http://40.68.2.184:8983/solr";
		HttpSolrClient cl = new HttpSolrClient.Builder(solrUrl).withConnectionTimeout(10000).withSocketTimeout(60000).build();
		
		
		final Map<String, String> queryParamMap = new HashMap<String, String>();
		queryParamMap.put("q", field+":"+value);
		MapSolrParams queryParams = new MapSolrParams(queryParamMap);

		final QueryResponse response = cl.query("cmdrs", queryParams);
		System.out.println("response: " + response);
		final SolrDocumentList documents = response.getResults();
		System.out.println("Found: " + documents.getNumFound());
		String responseString = StringUtils.EMPTY;
		for (SolrDocument document : documents) {
			responseString += document.jsonStr();
		}
		return responseString;
    }
    
    private String createDBEntry(HttpRequestMessage<Optional<String>>  request) throws SolrServerException, IOException {
    	CmdrsDBController cmdrsDBController = CmdrsDBController
				.getInstance();

		String id = generateUUID();
		String name = request.getQueryParameters().get("NAME");
		String value = request.getQueryParameters().get("VALUE");
		updateSolr(id, name, value);
		return gson.toJson(cmdrsDBController.createCmdrsEntry(name,value, true));
    }
    
    
    
    private boolean updateSolr(String id, String name, String value) throws SolrServerException, IOException {
		final String solrUrl = "http://40.68.2.184:8983/solr";
		HttpSolrClient cl = new HttpSolrClient.Builder(solrUrl).withConnectionTimeout(10000).withSocketTimeout(60000).build();
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("id",id);
		doc.addField("Name", name);
		doc.addField("Value", value);
		cl.add("cmdrs", doc);
		UpdateResponse response = cl.commit("cmdrs");
		int status = response.getStatus();
		if (status == 0) {
			return true;
		}
		return false;
    }
    
    private boolean updateSolr(SolrInputDocument document) throws SolrServerException, IOException {
		final String solrUrl = "http://40.68.2.184:8983/solr";
		HttpSolrClient cl = new HttpSolrClient.Builder(solrUrl).withConnectionTimeout(10000).withSocketTimeout(60000).build();
		cl.add("cmdrs", document);
		UpdateResponse response = cl.commit("cmdrs");
		int status = response.getStatus();
		if (status == 0) {
			return true;
		}
		return false;
    }
    
    
    private static String generateUUID() {
    	return UUID.randomUUID().toString();
    }
     	
    public static void main(String... args) {
    	try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("DBEntries.csv")));
			String str;
			while ((str = bufferedReader.readLine())!=null) {
				createDoc(str);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("Test111");
    }
    
    private void initDBAndSolr() {
    	try {
    		InputStreamReader inputStreamReader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("DBEntries.csv"));
    		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String str;
			CmdrsDBController cmdrsDBController = CmdrsDBController.getInstance();
			while ((str = bufferedReader.readLine())!=null) {
				updateSolr(createDoc(str));
				cmdrsDBController.createCmdrsEntry(createJson(str));
			}
		} catch (IOException | SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("Test111");
    }
    
   private static SolrInputDocument createDoc(String str) {
	   SolrInputDocument doc = new SolrInputDocument();
	   doc.addField("id",generateUUID());
	   int colCounter = 0;
	   for (String entry:str.split(",")) {
		   doc.addField("S"+colCounter, entry);
		   colCounter++;
	   }
	   System.out.println(doc);
	   return doc;
   }
   
   private static String createJson(String str) {
	   JsonObject json = new JsonObject();
	   json.addProperty("id", generateUUID());
	   int colCounter = 0;
	   for (String entry:str.split(",")) {
		   json.addProperty("S"+colCounter, entry);
		   colCounter++;
	   }
	  return json.toString();
   }
    
}
