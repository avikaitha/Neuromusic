package ark.neuromusic;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private List<Track> mListItems;
    private SCTrackAdapter mAdapter;
    private TextView mSelectedTrackTitle;
    private ImageView mSelectedTrackImage;
    private MediaPlayer mMediaPlayer;
    private ImageView mPlayerControl;
    private ListView mListView;
    final ArrayList<Track> mSoundCloudTracks = new ArrayList<>();

    private void getSoundtracks(String mood) {
        final ArrayList<String> trackslist = new ArrayList<>();
        TrackService musicoveryService = Musicovery.getService();

        musicoveryService.getNeuroTracks(mood, new Callback<JsonElement>() {
            @Override
            public void success(JsonElement tracks, Response response) {
                try {
                    JsonObject obj = tracks.getAsJsonObject();
                    tracks = obj.get("root")
                            .getAsJsonObject()
                            .get("tracks")
                            .getAsJsonObject()
                            .get("track")
                            .getAsJsonArray();

                    for (int i = 0; i < tracks.getAsJsonArray().size(); i++) {
                        String title = tracks.getAsJsonArray()
                                .get(i)
                                .getAsJsonObject()
                                .get("title")
                                .toString();
                        trackslist.add(title);
                        Log.d(TAG, trackslist.get(i));

                    }

                    TrackService soundCloudService = SoundCloud.getService();

                    for (int i = 0; i < trackslist.size(); i++) {

                        try {
                            soundCloudService.getSoundCloudTracks(trackslist.get(i), new Callback<List<Track>>() {
                                @Override
                                public void success(List<Track> tracks, Response response) {
                                    mSoundCloudTracks.add(tracks.get(0));
                                    if(mSoundCloudTracks.size()>0)
                                    Log.d(TAG,"SC TRAck: "+mSoundCloudTracks.get((mSoundCloudTracks.size()-1)).getTitle());
                                    loadTracks(mSoundCloudTracks);
                                    if(mSoundCloudTracks.size() == 1) {
                                        Track track = mSoundCloudTracks.get(0);

                                        mSelectedTrackTitle.setText(track.getTitle());
                                        Picasso.with(MainActivity.this)
                                                .load(track.getArtworkURL())
                                                .placeholder(getResources().getDrawable(R.drawable.placeholder_track_drawable))
                                                .into(mSelectedTrackImage);

                                        if (mMediaPlayer.isPlaying()) {
                                            mMediaPlayer.stop();
                                            mMediaPlayer.reset();
                                        }

                                        try {
                                            mMediaPlayer.reset();
                                            mMediaPlayer.setDataSource(track.getStreamURL() + "?client_id=" + Config.SC_CLIENT_ID);
                                            mMediaPlayer.prepareAsync();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override
                                public void failure(RetrofitError error) {
                                    Log.d(TAG, "Error: " + error);
                                }
                            });
                        } catch (ArrayIndexOutOfBoundsException e) {
                            Log.e(TAG, "Data not ready");
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "Callback Failed");
                error.printStackTrace();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
        getSoundtracks("chillout");
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                togglePlayPause();
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mPlayerControl.setImageResource(R.drawable.ic_play);
            }
        });

        mListItems = new ArrayList<>();
        mListView = (ListView)findViewById(R.id.track_list_view);
        mAdapter = new SCTrackAdapter(this, mListItems);
        mListView.setAdapter(mAdapter);

        mSelectedTrackTitle = (TextView)findViewById(R.id.selected_track_title);
        mSelectedTrackImage = (ImageView)findViewById(R.id.selected_track_image);
        mPlayerControl = (ImageView)findViewById(R.id.player_control);

        mPlayerControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Track track = mListItems.get(position);

                mSelectedTrackTitle.setText(track.getTitle());
                Picasso.with(MainActivity.this)
                        .load(track.getArtworkURL())
                        .placeholder(getResources().getDrawable(R.drawable.placeholder_track_drawable))
                        .into(mSelectedTrackImage);

                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                    mMediaPlayer.reset();
                }

                try {
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(track.getStreamURL() + "?client_id=" + Config.SC_CLIENT_ID);
                    mMediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
//        String[] fakeData = {"Rain Fall Down"
//                ,"She Builds Quick Machines"
//                ,"Little queen of spades","Can't Stop This Thing We Started"
//                ,"The Offspring"};




    }




    private void loadTracks(ArrayList<Track> tracks) {
        Log.d(TAG,"Inside LoadTracks");
        mListItems.clear();
        mListItems.addAll(tracks);
        mAdapter.notifyDataSetChanged();
    }

    private void togglePlayPause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mPlayerControl.setImageResource(R.drawable.ic_play);
        } else {
            mMediaPlayer.start();
            mPlayerControl.setImageResource(R.drawable.ic_pause);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.reset();
            mMediaPlayer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
