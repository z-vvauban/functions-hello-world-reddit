package functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.Map;

public class HelloWorld implements HttpFunction {


    // Simple function to return "Hello World"
    @Override
    public void service( HttpRequest request, HttpResponse response )
            throws IOException {
        try {
            String url = "https://oauth.reddit.com/r/java/new?limit=100";
            Map<String, String> headers = Map.of(
                    "User-Agent", "ChangeMeClient/0.1 by YourUsername",
                    "Authorization",
                    "bearer " + token
                                                );

            RedditData vvResponse = sendGetRequest( url, headers, RedditData.class );
            System.out.println( vvResponse );
            BufferedWriter writer = response.getWriter();
            writer.write( "" + vvResponse.getData()
                                         .getChildren()
                                         .stream()
                                         .map( child -> child.getData() )
                                         .filter( data -> data.getCreated()
                                                              .isAfter( LocalDate.now()
                                                                                 .minusWeeks( 1 ) ) )
                                         .map( data -> data.toString() )
                                         .count() );

        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }


    public RedditData sendGetRequest( String urlString, Map<String, String> headers, Class<RedditData> clazz ) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL( urlString );
        HttpURLConnection conn = ( HttpURLConnection ) url.openConnection();
        conn.setRequestMethod( "GET" );

        // Set headers
        for ( Map.Entry<String, String> entry : headers.entrySet() ) {
            conn.setRequestProperty( entry.getKey(), entry.getValue() );
        }

        RedditData vvResponse;
        try ( BufferedReader reader = new BufferedReader( new InputStreamReader( conn.getInputStream() ) ) ) {
            String line;
            while ( (line = reader.readLine()) != null ) {
                result.append( line );
            }
        }

        // Deserialize JSON response to RedditData object
        ObjectMapper mapper = new ObjectMapper();
        vvResponse = mapper.readValue( result.toString(), clazz );

        return vvResponse;
    }


}