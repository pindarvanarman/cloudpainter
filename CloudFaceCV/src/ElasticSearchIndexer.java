

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;



public class ElasticSearchIndexer
{

	
public static int count = 0;
public static int failcount = 0;
	
public static void main(String[] args) {
	BufferedReader br;
	try {
		br = new BufferedReader(new FileReader("pbp-2014.csv"));
	
		try {
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();
	
		    while (line != null) {
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    String everything = sb.toString();
		    
		    parseAndSaveCsv(everything);
		    
		    br.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

public static void parseAndSaveCsv(String filetext) {
	
	StringTokenizer rows = new StringTokenizer(filetext,"\n");
	System.out.println(rows.countTokens());
	
	String colstring = rows.nextToken();
	colstring = colstring.replaceAll(",,", ", ,");
	colstring = colstring.replaceAll(",,", ", ,");
	colstring = colstring.replaceAll("\n", "");
	colstring = colstring.replaceAll("\r", "");
	StringTokenizer cols = new StringTokenizer(colstring,",");
	System.out.println(cols.countTokens());
	int colCount = cols.countTokens();
	String[] headers = new String[colCount];
	for (int i=0; i<colCount; i++) {
		headers[i] = cols.nextToken().trim();
	}
	
	//For each row - split and print ElasticSearch Command
	int rowCount = rows.countTokens();
	String dateString = "";
	for (int j=0; j<rowCount; j++){
		
		String rawLine = rows.nextToken();
		//rawLine = rawLine.replaceAll("\n", " ");
		//rawLine = rawLine.replaceAll("\r", " ");
		//rowstring = rowstring.replaceAll(",,", ", ,");
		
        String line = rawLine;

        String otherThanQuote = " [^\"] ";
        String quotedString = String.format(" \" %s* \" ", otherThanQuote);
        String regex = String.format("(?x) "+ // enable comments, ignore white spaces
                ",                         "+ // match a comma
                "(?=                       "+ // start positive look ahead
                "  (?:                     "+ //   start non-capturing group 1
                "    %s*                   "+ //     match 'otherThanQuote' zero or more times
                "    %s                    "+ //     match 'quotedString'
                "  )*                      "+ //   end group 1 and repeat it zero or more times
                "  %s*                     "+ //   match 'otherThanQuote'
                "  $                       "+ // match the end of the string
                ")                         ", // stop positive look ahead
                otherThanQuote, quotedString, otherThanQuote);

        String[] values = line.split(regex, -1);
        //for(String t : tokens) {
            
        //}
		
		
		
        //String rawRowString =  rows.nextToken();
		//String rowstring = rawRowString;
		//rowstring = rowstring.replaceAll("\"", " ");
		//rowstring = rowstring.replaceAll(" ", " ");
		//rowstring = rowstring.replaceAll("'", " ");
		//rowstring = rowstring.replaceAll("\n", " ");
		//rowstring = rowstring.replaceAll("\r", " ");
		//rowstring = rowstring.replaceAll(",,", ", ,");
		//StringTokenizer theseValues = new StringTokenizer(rowstring,",");
		//colCount = theseValues.countTokens();
		//String[] values = new String[colCount];
		//for (int i=0; i<colCount; i++) {
			//values[i] = theseValues.nextToken();
			//if (i==0) {
				//dateString = values[i].substring(0,4)+"-"+values[i].substring(4,6)+"-"+values[i].substring(6,8);
			//}
		//}
        dateString = values[0].substring(0,4)+"-"+values[0].substring(4,6)+"-"+values[0].substring(6,8);
		values[1] = dateString;
		if(headers.length!=values.length) {
			for (int i=0;i<headers.length;i++){
				System.out.println(headers[i]+":"+values[i]);//"   "+count);
				
			}
		}
		for (int k=0;k<values.length;k++) {
			values[k] = values[k].replace("\"", " ");
		}
		sendToElasticSearch(headers, values);
	}
	
}

public static void sendToElasticSearch(String[] headers, String[] values) {
	try {
		StringBuffer esdoc = new StringBuffer();
		esdoc.append("{ ");
		for (int i=0;i<headers.length;i++) {
			if (headers[i].length()>1 && values[i].length()>=1 && !values[i].equals(" ")){
				String valueAsStringOrNumber = values[i];
				try {
					valueAsStringOrNumber = ""+Integer.parseInt(valueAsStringOrNumber);
				} catch (Exception e) {
					valueAsStringOrNumber = "\""+valueAsStringOrNumber+"\"";
				}
				esdoc.append("\""+headers[i]+"\":"+valueAsStringOrNumber+",");
			}
		}
		esdoc.delete(esdoc.length()-1,esdoc.length());
		esdoc.append(" }");
		String jsondoc = esdoc.toString();
		jsondoc = jsondoc.replaceAll(" ", "_");
		jsondoc = jsondoc.replaceAll("\\s", "");
		jsondoc = jsondoc.replaceAll("_", " ");
		jsondoc = jsondoc.replaceAll(" \"", "\"");
		jsondoc = jsondoc.replaceAll("\" ", "\"");

		
		
		String elasticEndPoint = "http://87b256626e4ad11cd37ac6d84dcba640.us-east-1.aws.found.io:9200/football2014/play/";
		String elasticUser =     "USERNAME:PASSWORD";
		System.out.println(jsondoc);
		sendPostRequest(elasticEndPoint, elasticUser, jsondoc);
	} catch (Exception e){
		
	}
}
	
public static String sendPostRequest(String requestUrl, String auth, String payload) {
	
 
    String authStr = auth;
    String authEncoded = Base64.encodeBytes(authStr.getBytes());

	
	StringBuffer jsonString = new StringBuffer();
	try {
        URL url = new URL(requestUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "Basic " + authEncoded);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        writer.write(payload);
        writer.close();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 
        String line;
        while ((line = br.readLine()) != null) {
                jsonString.append(line);
        }
        br.close();
        connection.disconnect();
        count++;
    } catch (Exception e) {
    	System.out.println("");
    	failcount++;
            //throw new RuntimeException(e.getMessage());
    }
	try {
		TimeUnit.MILLISECONDS.sleep(10);
	}
	catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	if (count%1000==1) {
		System.out.println(count);
	}
	
	return jsonString.toString();
	
    
    
}

//PostMethod post = new PostMethod("http://jakarata.apache.org/");
//NameValuePair[] data = {
//    new NameValuePair("user", "joe"),
//    new NameValuePair("password", "bloggs")
//};
//post.setRequestBody(data);


}
