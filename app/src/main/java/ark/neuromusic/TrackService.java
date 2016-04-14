package ark.neuromusic;

import com.google.gson.JsonElement;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by echessa on 6/18/15.
 */
public interface TrackService {

    @GET("/tracks?client_id=" + Config.SC_CLIENT_ID)
    public void getSoundCloudTracks(@Query("q") String trackName, Callback<List<Track>> cb);

    @GET("/playlist.php?fct=getfromtag&popularitymin=50&format=json")
    public void getNeuroTracks(@Query("tag") String mood, Callback<JsonElement> musicoverycb);



}