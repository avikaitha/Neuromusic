package ark.neuromusic;


import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.HEAD;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private List<Track> mListItems;


    private SCTrackAdapter mSCAdapter;

    private TextView mSelectedTrackTitle;
    private ImageView mSelectedTrackImage;
    private MediaPlayer mMediaPlayer;
    private ImageView mPlayerControl;
    private ListView mListView;

    private SlidingUpPanelLayout mLayout;
    final ArrayList<Track> mSoundCloudTracks = new ArrayList<>();

    int flag = 1;

    String TITLES[] = {"Home","Fav Artists","Playlists","Settings","Logout"};
    int ICONS[] = {R.drawable.artist_icon,R.drawable.artist_icon,R.drawable.artist_icon,R.drawable.artist_icon,R.drawable.artist_icon};

    //Similarly we Create a String Resource for the name and email in the header view
    //And we also create a int resource for profile picture in the header view


    private Toolbar toolbar;                              // Declaring the Toolbar Object

    RecyclerView mRecyclerView;                           // Declaring RecyclerView
    RecyclerView.Adapter mAdapter;                        // Declaring Adapter For Recycler View
    RecyclerView.LayoutManager mLayoutManager;            // Declaring Layout Manager as a linear layout manager
    DrawerLayout Drawer;                                  // Declaring DrawerLayout

    ActionBarDrawerToggle mDrawerToggle;                  // Declaring Action Bar Drawer Toggle

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
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }

                TrackService soundCloudService = SoundCloud.getService();

                for (int i = 0; i < trackslist.size(); i++) {

                    try {
                        soundCloudService.getSoundCloudTracks(trackslist.get(i), new Callback<List<Track>>() {
                            @Override
                            public void success(List<Track> tracks, Response response) {
                                try {
                                    mSoundCloudTracks.add(tracks.get(0));
                                    if (mSoundCloudTracks.size() > 0)
                                        Log.d(TAG, "SC TRAck: " + mSoundCloudTracks.get((mSoundCloudTracks.size() - 1)).getTitle());
                                    loadTracks(mSoundCloudTracks);
                                    if (mSoundCloudTracks.size() == 1) {

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
                                } catch (Resources.NotFoundException e) {
                                    e.printStackTrace();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (SecurityException e) {
                                    e.printStackTrace();
                                } catch (IndexOutOfBoundsException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Log.d(TAG, "Callback Failed");
                                error.printStackTrace();
                            }


                        });


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "Callback Failed");
                error.printStackTrace();
            }


        });
    }


    private boolean isInitial() {
        if(flag == 1) {
            return true;
        }
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String fb_name = intent.getStringExtra(LoginActivity.FB_NAME);
        String fb_dp = intent.getStringExtra(LoginActivity.FB_DP);
        String fb_cover = intent.getStringExtra(LoginActivity.FB_COVER);
        String email = intent.getStringExtra(LoginActivity.FB_EMAIL);


        mRecyclerView = (RecyclerView) findViewById(R.id.RecyclerView); // Assigning the RecyclerView Object to the xml View

        mRecyclerView.setHasFixedSize(true);                            // Letting the system know that the list objects are of fixed size

        mAdapter = new MyAdapter(TITLES,ICONS,fb_name,email,fb_dp,this);       // Creating the Adapter of MyAdapter class(which we are going to see in a bit)
        // And passing the titles,icons,header view name, header view email,
        // and header view profile picture

        mRecyclerView.setAdapter(mAdapter);
        // Setting the adapter to RecyclerView

        final GestureDetector mGestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {

            @Override public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

        });


        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {
                View child = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());


                if (child != null && mGestureDetector.onTouchEvent(motionEvent)) {
                    Drawer.closeDrawers();
                    Toast.makeText(MainActivity.this, "The Item Clicked is: " + recyclerView.getChildPosition(child), Toast.LENGTH_SHORT).show();

                    return true;

                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });


        mLayoutManager = new LinearLayoutManager(this);                 // Creating a layout Manager

        mRecyclerView.setLayoutManager(mLayoutManager);                 // Setting the layout Manager


        Drawer = (DrawerLayout) findViewById(R.id.DrawerLayout);        // Drawer object Assigned to the view
        mDrawerToggle = new ActionBarDrawerToggle(this,Drawer,toolbar,R.string.openDrawer,R.string.closeDrawer){

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // code here will execute once the drawer is opened( As I dont want anything happened whe drawer is
                // open I am not going to put anything here)
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                // Code here will execute once drawer is closed
            }



        }; // Drawer Toggle Object Made
        Drawer.setDrawerListener(mDrawerToggle); // Drawer Listener set to the Drawer toggle
        mDrawerToggle.syncState();               // Finally we set the drawer toggle sync State

        getSoundtracks("chillout");
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                if(!isInitial()) {
                    togglePlayPause();

                }
                else {
                    mPlayerControl.setImageResource(R.drawable.ic_play);
                    flag++;
                }

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

        mSCAdapter = new SCTrackAdapter(this, mListItems);
        mListView.setAdapter(mSCAdapter);

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


        final Toolbar mSlidingToolbar = (Toolbar) findViewById(R.id.slidingToolbar);
        final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mSlidingToolbar.getLayoutParams();
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.slidingLayout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if(slideOffset >= 0.9) {
                    params.setMargins(0,getStatusBarHeight(),0,0);
                    mSlidingToolbar.setLayoutParams(params);
                }
                else {
                    params.setMargins(0,0,0,0);
                    mSlidingToolbar.setLayoutParams(params);
                }
            }

            @Override
            public void onPanelStateChanged(View view, SlidingUpPanelLayout.PanelState panelState, SlidingUpPanelLayout.PanelState panelState1) {

            }


        });
        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });



    }




    private void loadTracks(ArrayList<Track> tracks) {
        Log.d(TAG,"Inside LoadTracks");
        mListItems.clear();
        mListItems.addAll(tracks);
        mSCAdapter.notifyDataSetChanged();
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


    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

}
