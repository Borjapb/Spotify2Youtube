import java.net.*;
import java.util.*;

import org.json.*;

import java.io.*;

import com.google.api.*;


/*
 * 1. Get a list of the user's Spotify playlists
 * 2. Extract from the Spotify playlist the tracks
 * */

public class ABC {

	private String spotify_user_id = "borjapb99";
	private String spotify_token = "BQC0159wlTJJ0T_OR5PYMPPV1Nk4Dsl01TVgd3sQlUePpiiVL-Y5bQb9jxA_DrAKE5S7BtWH4wFhpm0wZ5MK8mlR17vHZSlLrbbj5PBuVvxH3CrsII8rpT58lJ-b6OA9ULEMIOW6SiNvw6JRA6IhqSGJiTHvTwAp_Zl3OV-8KAO74CZz49f3l1xPejiV3pqjKLJsdmBcFEtBO8MH3g";
	
	private static final String CLIENT_SECRETS= "client_secret.json";
    private static final Collection<String> SCOPES =
        Arrays.asList("https://www.googleapis.com/auth/youtube " +
        "https://www.googleapis.com/auth/youtube.force-ssl " +
        "https://www.googleapis.com/auth/youtubepartner");
    
    private static final String APPLICATION_NAME = "API code samples";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	
	public ABC() {}
	
	/*
	 * 
	 */
	public String send_GET(String url) throws Exception{
		//Create connection
		
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
	    
		connection.setRequestMethod("GET");
	    
		//add request header
	    connection.setRequestProperty(
	    		"Authorization", "Bearer " + this.spotify_token
	    );
	    
	    //send request
	    int responseCode = connection.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);
        
        //get the response in a String(response)
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        	
            String line;

            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
	}
	
	/* String get_Spotify_playlist()
	 * 
	 * Returns String with the playlist id of the Spotify user's account
	 * */	
	public String get_Spotify_playlist() throws Exception{
		
		int i;
		
		String url = String.format("https://api.spotify.com/v1/users/%s/playlists?limit=50", this.spotify_user_id);
		//send the GET request
		String response = this.send_GET(url);
		
        //transformation of a String to JSONObject
        JSONObject jsonResponse = new JSONObject(response);
        
        //manipulate a JSON array
        JSONArray items_array = new JSONArray(jsonResponse.getJSONArray("items").toString());
        
        //creation of a dictionary with a id(key) and another dictionary(value) with the name of the playlist(key)
        //and the playlistId(value)
        HashMap<Integer, HashMap<String, String>> map = new HashMap<Integer, HashMap<String,String>>();
        
        //filling the dictionary
        for(i = 0 ; i < items_array.length() ; i++) {
        	
        	JSONObject aux = items_array.getJSONObject(i);
        	map.put((i+1), new HashMap<String, String>());
        	map.get(i+1).put(aux.getString("name"), aux.getString("id"));
        	
        	//print the options for the user to choose
        	System.out.println((i+1) + ". " + aux.getString("name"));
        }
        
        String playlist_number;
        int n;
        do {
	        //ask the option to the user
	        Scanner myObj = new Scanner(System.in);
	        
	        System.out.println("Enter the number of the playlist to choose: "); 
	        playlist_number = myObj.nextLine();
	        n = Integer.parseInt(playlist_number);
        
        }while(n < 1 || n > (items_array.length()+1));
        
        //get the pair: {playlist_name=playlistId}
        String auxString = map.get(n).toString();
        int beginIndex = 0;
        for(i = 0 ; i < auxString.length() ; i++) {
        	if(auxString.charAt(i) == '=') {
        		beginIndex = i+1;
        		break;
        	}
        }
        
        //extract the playlistId from the pair
		return auxString.substring(beginIndex, auxString.length()-1);
	}
	
	/*
	 * 
	 */
	public void get_playlist_tracks(String playlist_id) throws Exception{
		
		int i, j;
		
		//Create connection
		String url = String.format("https://api.spotify.com/v1/playlists/%s/tracks", playlist_id);
		String response = this.send_GET(url);
		
		//transformation of a String to JSONObject
        JSONObject jsonResponse = new JSONObject(response.toString());
        
        //get the array of tracks
        JSONArray items_array = new JSONArray(jsonResponse.getJSONArray("items").toString());
        
        Map<String, String> song_data = new HashMap<>();
        
        String song_name;
        String artists = "";
        
        //obtain song name and artists and store them in a map        
        for(i = 0 ; i < items_array.length() ; i++) {
        	
        	JSONObject aux = items_array.getJSONObject(i);
        	
        	JSONObject aux2 = new JSONObject(aux.getJSONObject("track").toString());
        	
        	JSONArray artists_array = new JSONArray(aux2.getJSONArray("artists").toString());
        	
        	for(j = 0 ; j < artists_array.length() ; j++) {
        		
        		JSONObject aux3 = artists_array.getJSONObject(j);
        		
        		if (j == 0)
        			artists = aux3.getString("name");
        		else
        			artists = artists + ", " + aux3.getString("name");
        		
        	}
        	song_name = aux2.getString("name");
        	
        	song_data.put(song_name, artists);
        }
        
	}
	
	/*
	 * 
	 */
	public void create_YT_playlist() {
		
	}
	public void add_videos_to_playlist() {
		
	}
	
	public static void main(String[] args) throws Exception {
		
		ABC p = new ABC();
		
		String playlist_id = p.get_Spotify_playlist();
		
		p.get_playlist_tracks(playlist_id);
	}
}
