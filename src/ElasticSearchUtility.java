//import Base64;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class ElasticSearchUtility
{

	public static void SendStrokesToElasticSearchCluster(String indexName, int strokeNumber, double startX, double startY, double endX, double endY, int width, int height, String painting) {
		StringBuffer json = new StringBuffer();
		json.append( "{ ");
		json.append( "\"paintingName\":\""+painting+"\"");
		json.append( ",\"canvasHeight\":"+height);
		json.append( ",\"canvasWidth\":"+width);
		json.append( ",\"strokeNumber\":"+strokeNumber);
		json.append( ",\"id\":"+strokeNumber);
		json.append( ",\"source\":\"CloudFaceLines\"");
		json.append( ",\"length\":1");
		json.append( ",\"timeStamp\":"+System.currentTimeMillis());
		json.append( ",\"color\":\"#000000\"");
		json.append( ",\"brushSize\":1\"");
		json.append( ",\"numberPoints\":2");
		json.append( ","+getJsonGeometricStrokeDescription(startX, startY,endX, endY) );
		json.append( " }");
		
		System.out.println(json.toString());
		sendToElasticSearch(1, painting, ""+strokeNumber, json.toString());
		
	}
	
	public static String getJsonGeometricStrokeDescription(double startX, double startY, double endX, double endY) {
		int numberPoints = 2;

		double maxX = startX;
		double minX = startX;
		double totalX = startX;

		double maxY = startY;
		double minY = startY;
		double totalY = startY;
		
		
			double thisX = endX;
			double thisY = endY;
			if ( thisX > maxX) {
				maxX = thisX;
			}
			if ( thisX < minX) {
				minX = thisX;
			}
			totalX += thisX;
			if ( thisY > maxY) {
				maxY = thisY;
			}
			if ( thisY < minY) {
				minY = thisY;
			}
			totalY += thisY;

		double midX = (maxX+minX)/2;
		double averageX = totalX/numberPoints;
		double midY = (maxY+minY)/2;
		double averageY = totalY/numberPoints;

		double slope = 0;
		try {
			slope = (maxY-minY)/(maxX-minX);
		} catch (Exception e) {
			slope = 1000000000;
		}
		
		int decPlaces = 3;

		StringBuffer json = new StringBuffer();
		json.append( "\"firstPoint\":["+round(startX,decPlaces)+","+round(startY,decPlaces)+"]") ;
		json.append( ",\"lastPoint\":["+round(endX,decPlaces) +","+round(endY,decPlaces)+"]") ;
		json.append( ",\"pathBound\":[ ["+round(minX,decPlaces)+","+round(maxY,decPlaces)+"],["+round(maxX,decPlaces)+","+round(maxY,decPlaces)+"],["+round(maxX,decPlaces)+","+round(minY,decPlaces)+"],["+round(minX,decPlaces)+","+round(minY,decPlaces)+"] ]" );
		json.append( ",\"pathBoundCenter\":["+round((midX),decPlaces)+","+round((midY),decPlaces)+" ]");
		json.append( ",\"pathAveragePoint\":["+round((averageX),decPlaces)+","+round((averageY),decPlaces)+" ]");
		json.append( ",\"path\":[ ");


			json.append("["+round(startX,decPlaces)+","+round(startY,decPlaces)+"]");
			json.append("["+round(endX,decPlaces)+","+round(endY,decPlaces)+"]");

			json.append( " ]");
		
		//yyyy/MM/dd HH:mm:ss
		
		Date thisDate = new Date(System.currentTimeMillis());
		 Calendar cal = Calendar.getInstance();
	    cal.setTime(thisDate);
	    int year = cal.get(Calendar.YEAR);
	    int month = cal.get(Calendar.MONTH)+1;
	    int day = cal.get(Calendar.DAY_OF_MONTH);
		String thisYearString = ""+year;
		String thisMonthString = ""+month;
		String thisDayString = ""+day;
	    
		int thisHour = cal.get(Calendar.HOUR_OF_DAY);
		int thisMinute = cal.get(Calendar.MINUTE);
		int thisSecond = cal.get(Calendar.SECOND);
		String thisHourString = ""+thisHour;
		String thisMinuteString = ""+thisMinute;
		String thisSecondString = ""+thisSecond;
		
		if ((month)<10) {
			thisMonthString = "0"+(month);
		}
		if (day<10) {
			thisDayString = "0"+day;
		}
		
		if (thisHour<10) {
			thisHourString = "0"+thisHour;
		}
		if (thisMinute<10) {
			thisMinuteString = "0"+thisMinute;
		}
		if (thisSecond<10) {
			thisSecondString = "0"+thisSecond;
		}
		
	    String dateString = ""+thisYearString+"/"+(thisMonthString)+"/"+thisDayString+" "+thisHourString+":"+thisMinuteString+":"+thisSecondString;
				
    	json.append( ",\"startDate\":\""+dateString+"\"");
		json.append( ",\"startTime\":"+((System.currentTimeMillis())));
		json.append( ",\"duration\":1");
		
		return json.toString();
	}
	



public static void sendToElasticSearch(int paintingId, String paintingName, String strokeId, String payload) {
	
	
	
	try {
		String elasticEndPoint = "http://87b256626e4ad11cd37ac6d84dcba640.us-east-1.aws.found.io:9200/strokes_"+paintingIdInMillion(paintingId)+"_"+paintingName.toLowerCase()+"/line/"+strokeId;
		String elasticUser =     "USERNAME:PASSWORD";
		sendPostRequest(elasticEndPoint, elasticUser, payload);
	} catch (Exception e){
		
	}
}

public static String paintingIdInMillion(int id) {
	
	if (id<10) {
		return "00000000"+id;
	}
	if (id<100) {
		return "0000000"+id;
	}
	if (id<1000) {
		return "000000"+id;
	}
	if (id<10000) {
		return "00000"+id;
	}
	if (id<100000) {
		return "0000"+id;
	}
	if (id<1000000) {
		return "000"+id;
	}
	if (id<10000000) {
		return "00"+id;
	}
	if (id<100000000) {
		return "0"+id;
	}
	return ""+id;
	
}

public static double round(double number, int places) {
	double roundedNumber;
	int tempRoundNumber;
	double multiplier = 1;
	for (int i = 0; i<places; i++) {
		multiplier=multiplier*10;
	}
	roundedNumber = number * multiplier;
	tempRoundNumber = (int)Math.round(roundedNumber);
	roundedNumber = ((double)tempRoundNumber)/multiplier;		
	return roundedNumber;
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
       // count++;
    } catch (Exception e) {
    	System.out.println("");
    	//failcount++;
            //throw new RuntimeException(e.getMessage());
    }
	try {
		TimeUnit.MILLISECONDS.sleep(10);
	}
	catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	//if (count%1000==1) {
	//	System.out.println(count);
	//}
	
	return jsonString.toString();
	
    
    
}


}




