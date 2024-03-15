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

    String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6IlNIQTI1NjpzS3dsMnlsV0VtMjVmcXhwTU40cWY4MXE2OWFFdWFyMnpLMUdhVGxjdWNZIiwidHlwIjoiSldUIn0.eyJzdWIiOiJ1c2VyIiwiZXhwIjoxNzEwNTE1MjE3Ljc5NDkxMiwiaWF0IjoxNzEwNDI4ODE3Ljc5NDkxMiwianRpIjoiVHhPbEg0TFYwcG5LSjRIVHRJVGxiczdRV2JFdXN3IiwiY2lkIjoiX2RUZmFfTnlWbVgxOXlTTUtKYjBNQSIsImxpZCI6InQyX3czNWNlOWQxbiIsImFpZCI6InQyX3czNWNlOWQxbiIsImxjYSI6MTcxMDMxOTQzMjU3Nywic2NwIjoiZUp5S1Z0SlNpZ1VFQUFEX193TnpBU2MiLCJmbG8iOjl9.pNbpbPBf-sNyFIj6t0osNyvhd4kU_B7ZkUXEaooP8HICQz2BbvFujD_Q4BuNo-jPUqqfN5GgDttGwAM13F7D0NEAMrzfyb1zyyo2C9ro4z9cYQ1V0xfKz909LuVNRGqYpMZZyb0iO5p0XfisFODMDUdfBAn0k5bl0f9bdqOAeVrmwenBFfV7qB5Xm51pyMbIV1eHMCQF04hck95MYqfy20AIzL7EKkE-fopCf7b9sVSsALqiw4deneGSz38BgWvnuPyNe7faCLTyagsUi9Q4CO2jkxeKdfOrfXiEFCSf1_ty6G1sD7sZWnL2lgtOGsPjxt7HJ5Yly84ztzCzhIeoAg";

    // Simple function to return "Hello World"
    @Override
    public void service( HttpRequest request, HttpResponse response )
            throws IOException {
        try {
            String messagesUrl = "https://oauth.reddit.com/r/java/new?limit=100";
            String aboutUrl = "https://oauth.reddit.com/r/java/about";
            Map<String, String> headers = Map.of(
                    "User-Agent", "ChangeMeClient/0.1 by YourUsername",
                    "Authorization",
                    "bearer " + token
                                                );

            RedditMessagesData vvResponse = sendMessagesGetRequest( messagesUrl, headers, RedditMessagesData.class );
            final String count = "" + vvResponse.getData()
                                                .getChildren()
                                                .stream()
                                                .map( RedditMessagesData.Child::getData )
                                                .filter( data -> data.getCreated()
                                                                     .isAfter( LocalDate.now()
                                                                                        .minusWeeks( 1 ) ) )
                                                .map( Object::toString )
                                                .count();
            RedditAboutData redditAboutData = sendAboutGetRequest( aboutUrl, headers, RedditAboutData.class );
            final String subscribers = "" + redditAboutData.getData()
                                                           .getSubscribers();
            BufferedWriter writer = response.getWriter();
            writer.write( "messages count:[" + count + "], subscribers count:[" + subscribers + "]" );

        } catch ( Exception e ) {
            e.printStackTrace();
        }

    }


    public RedditMessagesData sendMessagesGetRequest( String urlString, Map<String, String> headers, Class<RedditMessagesData> clazz ) throws
                                                                                                                                       IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL( urlString );
        HttpURLConnection conn = ( HttpURLConnection ) url.openConnection();
        conn.setRequestMethod( "GET" );

        // Set headers
        for ( Map.Entry<String, String> entry : headers.entrySet() ) {
            conn.setRequestProperty( entry.getKey(), entry.getValue() );
        }

        RedditMessagesData vvResponse;
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

    public RedditAboutData sendAboutGetRequest( String urlString, Map<String, String> headers, Class<RedditAboutData> clazz ) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL( urlString );
        HttpURLConnection conn = ( HttpURLConnection ) url.openConnection();
        conn.setRequestMethod( "GET" );

        // Set headers
        for ( Map.Entry<String, String> entry : headers.entrySet() ) {
            conn.setRequestProperty( entry.getKey(), entry.getValue() );
        }

        RedditAboutData vvResponse;
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