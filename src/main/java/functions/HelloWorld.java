package functions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelloWorld implements HttpFunction {

    List<String> topics = List.of( "java", "javascript", "Python", "csharp" );

    // Simple function to return "Hello World"
    @Override
    public void service( HttpRequest request, HttpResponse response )
            throws IOException {
        try {
            String tokenUrl = "https://www.reddit.com/api/v1/access_token";
            Map<String, String> tokenHeaders = new HashMap<>();
            tokenHeaders.put( "Content-Type", "application/x-www-form-urlencoded" );
            tokenHeaders.put( "Authorization", "Basic X19kY0xESHZyM1Rkb2VxSkh6UWpuUTpTTVpZaTVSUHF4QWVlMDRKNEhKcGZyRGRfUUhxNEE=" );

            Map<String, String> formData = new HashMap<>();
            formData.put( "grant_type", "password" );
            formData.put( "username", "Zreddit59" );
            formData.put( "password", "codeTheWorld" );

            // Call the method with the URL and the maps
            RedditToken redditToken = sendHttpRequest( "https://www.reddit.com/api/v1/access_token", tokenHeaders, formData, RedditToken.class,
                                                       "POST" );


            String output = "";

            for ( String topic : topics ) {
                String messagesUrl = "https://oauth.reddit.com/r/" + topic + "/new?limit=100";

                String aboutUrl = "https://oauth.reddit.com/r/" + topic + "/about";

                Map<String, String> headers = Map.of(
                        "User-Agent", "ChangeMeClient/0.1 by YourUsername",
                        "Authorization",
                        "bearer " + redditToken.getToken() );
                RedditMessagesData messageResponse = sendHttpRequest( messagesUrl, headers, null, RedditMessagesData.class, "GET" );
                final String messagesCount = "" + messageResponse.getData()
                                                                 .getChildren()
                                                                 .stream()
                                                                 .map( RedditMessagesData.Child::getData )
                                                                 .filter( data -> data.getCreated()
                                                                                      .isAfter( LocalDate.now()
                                                                                                         .minusWeeks( 1 ) ) )
                                                                 .count();
                RedditAboutData aboutResponse = sendHttpRequest( aboutUrl, headers, null, RedditAboutData.class, "GET" );
                final String subscribersCount = "" + aboutResponse.getData()
                                                                  .getSubscribers();
                output = output + " | " + topic + " messages=[" + messagesCount + "] subscribers=[" + subscribersCount + "]\n";
            }

            BufferedWriter writer = response.getWriter();
            writer.write( output );

        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }

    public <T> T sendHttpRequest( String urlString, Map<String, String> headers, Map<String, String> formData, Class<T> clazz, String requestMethod ) throws
                                                                                                                                                      IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL( urlString );
        HttpURLConnection conn = ( HttpURLConnection ) url.openConnection();
        conn.setRequestMethod( requestMethod );

        // Set headers
        for ( Map.Entry<String, String> entry : headers.entrySet() ) {
            conn.setRequestProperty( entry.getKey(), entry.getValue() );
        }

        // If the request method is POST, handle form data
        if ( "POST".equals( requestMethod ) ) {
            conn.setDoOutput( true ); // Needed to send the request body
            StringBuilder postData = new StringBuilder();
            for ( Map.Entry<String, String> entry : formData.entrySet() ) {
                if ( postData.length() != 0 ) {
                    postData.append( '&' );
                }
                postData.append( URLEncoder.encode( entry.getKey(), "UTF-8" ) );
                postData.append( '=' );
                postData.append( URLEncoder.encode( entry.getValue(), "UTF-8" ) );
            }
            // Send form data
            try ( DataOutputStream wr = new DataOutputStream( conn.getOutputStream() ) ) {
                wr.writeBytes( postData.toString() );
            }
        }

        T response;
        // Read the response
        try ( BufferedReader reader = new BufferedReader( new InputStreamReader( conn.getInputStream() ) ) ) {
            String line;
            while ( (line = reader.readLine()) != null ) {
                result.append( line );
            }
        }

        // Deserialize JSON response to the specified class object
        ObjectMapper mapper = new ObjectMapper();
        response = mapper.readValue( result.toString(), clazz );

        return response;
    }

}