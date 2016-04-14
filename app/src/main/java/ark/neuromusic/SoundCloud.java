package ark.neuromusic;

import retrofit.RestAdapter;

/**
 * Created by avinash on 4/8/16.
 */
public class SoundCloud {

    private static final RestAdapter REST_ADAPTER = new RestAdapter.Builder().setEndpoint(Config.SC_API_URL).build();
    private static final TrackService SERVICE = REST_ADAPTER.create(TrackService.class);

    public static TrackService getService() {
        return SERVICE;
    }
}
