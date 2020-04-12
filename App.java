package com.codebind;


import java.net.*;
import java.util.*;

import java.io.*;

import org.json.*;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.PlaylistStatus;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.common.collect.Lists;


public class App {

	/******SPOTIFY******/
	private String spotify_user_id = "COMPLETAR";
	private String spotify_token = "COMPLETAR";
	/*******************/

	/******YOUTUBE******/
	/** Global instance of the HTTP transport. */
	/** Global instance of the HTTP transport. */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	/** Global instance of YouTube object to make all API requests. */
	private static YouTube youtube;

	/*******************/


	private Map<String, String> song_data;


	public App() {}

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

	/* String get_Spotify_playlists()
	 * 
	 * Returns String with the playlist id of the Spotify user's account
	 * */	
	public String get_Spotify_playlists() throws Exception{

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

		song_data = new HashMap<>();

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
	/**
	 * Authorizes the installed application to access user's protected data.
	 *
	 * @param scopes list of scopes needed to run upload.
	 */
	private static Credential authorize(List<String> scopes) throws Exception {

		// Load client secrets.
		GoogleClientSecrets clientSecrets =
				GoogleClientSecrets.load(
						JSON_FACTORY,
						App.class.getResourceAsStream("client_secrets.json")
						);

		// Checks that the defaults have been replaced (Default = "Enter X here").
		if (clientSecrets.getDetails().getClientId().startsWith("Enter")
				|| clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
			System.out.println(
					"Enter Client ID and Secret from https://console.developers.google.com/project/_/apiui/credential"
							+ "into youtube-cmdline-playlistupdates-sample/src/main/resources/client_secrets.json");
			System.exit(1);
		}

		// Set up file credential store.
		FileCredentialStore credentialStore =
				new FileCredentialStore(
						new File(System.getProperty("user.home"),
								".credentials/youtube-api-playlistupdates.json"),
						JSON_FACTORY);

		// Set up authorization code flow.
		GoogleAuthorizationCodeFlow flow = 
				new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, 
						JSON_FACTORY,
						clientSecrets,
						scopes)
				.setCredentialStore(credentialStore).build();

		// Build the local server and bind it to port 9000
		LocalServerReceiver localReceiver = new LocalServerReceiver.Builder().setPort(8080).build();

		// Authorize.
		return new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user");
	}


	public ArrayList<String> getVideosIds() throws IOException{

		Properties properties = new Properties();
		ArrayList<String> videoIds = new ArrayList<>();

		for (Map.Entry<String,String> entry : this.song_data.entrySet()) {  

			YouTube.Search.List search = youtube.search().list("id,snippet");
			/*
			 * It is important to set your API key from the Google Developer Console for
			 * non-authenticated requests (found under the Credentials tab at this link:
			 * console.developers.google.com/). This is good practice and increased your quota.
			 */
			String apiKey = properties.getProperty("youtube.apikey");
			search.setKey(apiKey);

			search.setQ(entry.getKey() + entry.getValue());
			/*
			 * We are only searching for videos (not playlists or channels). If we were searching for
			 * more, we would add them as a string like this: "video,playlist,channel".
			 */
			search.setType("video");
			/*
			 * This method reduces the info returned to only the fields we need and makes calls more
			 * efficient.
			 */
			search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
			search.setMaxResults(1L);
			SearchListResponse searchResponse = search.execute();

			Iterator<SearchResult> searchResultList = searchResponse.getItems().iterator();

			if(searchResultList.hasNext()) {
				SearchResult singleVideo = searchResultList.next();
				ResourceId rId = singleVideo.getId();
				videoIds.add(rId.getVideoId());
			}
		}

		return videoIds;
	}

	/*
	 * Authorizes user, runs Youtube.Channnels.List get the playlist id associated with uploaded
	 * videos, runs YouTube.PlaylistItems.List to get information on each video, and prints out the
	 * results.
	 */
	public void create_YT_playlist() {
		// General read/write scope for YouTube APIs.
		List<String> scopes = Lists.newArrayList("https://www.googleapis.com/auth/youtube");

		try {
			// Authorization.
			Credential credential = authorize(scopes);

			// YouTube object used to make all API requests.
			youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
					.setApplicationName("youtube-cmdline-playlistupdates-sample")
					.build();

			// Creates a new playlist in the authorized user's channel.
			String playlistId = insertPlaylist();

			System.out.println("Obteniendo las IDs de los videos...");
			// Get video's IDs to insert into the playlist
			List<String> videoIds = this.getVideosIds();

			System.out.println("Añadiendo los videos a la playlist...");
			for(String id: videoIds) {
				// If a valid playlist was created, adds a new playlistitem with a video to that playlist.
				insertPlaylistItem(playlistId, id);
			}

		} catch (GoogleJsonResponseException e) {
			System.err.println("There was a service error: " + e.getDetails().getCode() + " : " + e.getDetails().getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			e.printStackTrace();
		} catch (Throwable t) {
			System.err.println("Throwable: " + t.getMessage());
			t.printStackTrace();
		}
	}

	/**
	 * Creates YouTube Playlist and adds it to the authorized account.
	 */
	private static String insertPlaylist() throws IOException {

		/*
		 * We need to first create the parts of the Playlist before the playlist itself.  Here we are
		 * creating the PlaylistSnippet and adding the required data.
		 */
		PlaylistSnippet playlistSnippet = new PlaylistSnippet();

		playlistSnippet.setTitle("Test Playlist " + Calendar.getInstance().getTime());
		playlistSnippet.setDescription("A public playlist created with the YouTube API v3");

		// Here we set the privacy status (required).
		PlaylistStatus playlistStatus = new PlaylistStatus();
		playlistStatus.setPrivacyStatus("public");

		/*
		 * Now that we have all the required objects, we can create the Playlist itself and assign the
		 * snippet and status objects from above.
		 */
		Playlist youTubePlaylist = new Playlist();
		youTubePlaylist.setSnippet(playlistSnippet);
		youTubePlaylist.setStatus(playlistStatus);

		/*
		 * This is the object that will actually do the insert request and return the result.  The
		 * first argument tells the API what to return when a successful insert has been executed.  In
		 * this case, we want the snippet and contentDetails info.  The second argument is the playlist
		 * we wish to insert.
		 */
		YouTube.Playlists.Insert playlistInsertCommand =
				youtube.playlists().insert("snippet,status", youTubePlaylist);
		Playlist playlistInserted = playlistInsertCommand.execute();

		// Pretty print results.

		System.out.println("New Playlist name: " + playlistInserted.getSnippet().getTitle());
		System.out.println(" - Privacy: " + playlistInserted.getStatus().getPrivacyStatus());
		System.out.println(" - Description: " + playlistInserted.getSnippet().getDescription());
		System.out.println(" - Posted: " + playlistInserted.getSnippet().getPublishedAt());
		System.out.println(" - Channel: " + playlistInserted.getSnippet().getChannelId() + "\n");
		return playlistInserted.getId();


	}
	/**
	 * Creates YouTube PlaylistItem with specified video id and adds it to the specified playlist id
	 * for the authorized account.
	 *
	 * @param playlistId assign to newly created playlistitem
	 * @param videoId YouTube video id to add to playlistitem
	 */
	private static String insertPlaylistItem(String playlistId, String videoId) throws IOException {

		/*
		 * The Resource type (video,playlist,channel) needs to be set along with the resource id. In
		 * this case, we are setting the resource to a video id, since that makes sense for this
		 * playlist.
		 */
		ResourceId resourceId = new ResourceId();
		resourceId.setKind("youtube#video");
		resourceId.setVideoId(videoId);

		/*
		 * Here we set all the information required for the snippet section.  We also assign the
		 * resource id from above to the snippet object.
		 */
		PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
		playlistItemSnippet.setTitle("First video in the test playlist");
		playlistItemSnippet.setPlaylistId(playlistId);
		playlistItemSnippet.setResourceId(resourceId);

		/*
		 * Now that we have all the required objects, we can create the PlaylistItem itself and assign
		 * the snippet object from above.
		 */
		PlaylistItem playlistItem = new PlaylistItem();
		playlistItem.setSnippet(playlistItemSnippet);

		/*
		 * This is the object that will actually do the insert request and return the result.  The
		 * first argument tells the API what to return when a successful insert has been executed.  In
		 * this case, we want the snippet and contentDetails info.  The second argument is the
		 * playlistitem we wish to insert.
		 */
		YouTube.PlaylistItems.Insert playlistItemsInsertCommand =
				youtube.playlistItems().insert("snippet,contentDetails", playlistItem);
		PlaylistItem returnedPlaylistItem = playlistItemsInsertCommand.execute();

		// Pretty print results.

		System.out.println("New PlaylistItem name: " + returnedPlaylistItem.getSnippet().getTitle());
		System.out.println(" - Video id: " + returnedPlaylistItem.getSnippet().getResourceId().getVideoId());
		System.out.println(" - Posted: " + returnedPlaylistItem.getSnippet().getPublishedAt());
		System.out.println(" - Channel: " + returnedPlaylistItem.getSnippet().getChannelId());
		return returnedPlaylistItem.getId();

	}

	public static void main(String[] args) throws Exception {

		App p = new App();

		//print the list of the playlists of the user
		String playlist_id = p.get_Spotify_playlists();

		//get all the tracks of the playlist
		p.get_playlist_tracks(playlist_id);

		p.create_YT_playlist();

	}
}

