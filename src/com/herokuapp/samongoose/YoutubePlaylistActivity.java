package com.herokuapp.samongoose;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.codebutler.android_websockets.SocketIOClient;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class YoutubePlaylistActivity extends Activity {
    

    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void goTo(View view) {
        String playlistId = ((EditText)findViewById(R.id.playlistID)).getText().toString();
        String playlist = "http://samongoose.herokuapp.com/Playlists/" + playlistId;
        String socketURL = "http://samongoose.herokuapp.com/?playlistid=" + playlistId;
        //String playlist = "http://10.0.2.2:8888/Playlists/" + playlistId;
        //String socketURL = "http://10.0.2.2:8888?playlistid=" + playlistId;
        openPlaylist(playlist, socketURL);
    }
    
    public void openPlaylist (String playlist, String socketURL)
    {
        Intent intent = new Intent(this, ViewPlaylistActivity.class);
        intent.putExtra(ViewPlaylistActivity.PLAYLIST_URL, playlist);
        intent.putExtra(ViewPlaylistActivity.SOCKET_URL, socketURL);
        startActivity(intent);
    }
    
    public void newPlaylist(View view) {
        final YoutubePlaylistActivity backPtr = this;
        //Thread t = new Thread(new Runnable() {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void...t) {
                HttpClient client = new DefaultHttpClient();
                HttpPost req = new HttpPost("http://samongoose.herokuapp.com/Playlists/");
                HttpResponse resp = null;
                try {
                    resp = client.execute(req);
                    if (resp.getStatusLine().getStatusCode() == 201)
                    {
                        String newLocation = resp.getHeaders("Location")[0].getValue();
                        return newLocation;
                    }
                } catch (Exception e)
                {
                    System.out.println(e.toString());
                }
                return "";
            }
            
            public void onPostExecute(String result) {
                Pattern p = Pattern.compile("Playlists/([0-9]+)/");
                Matcher m = p.matcher(result);
                String playlistid = m.group(1);
                String socketURL = URI.create(result).getHost() + "?playlistid=" + playlistid;
                backPtr.openPlaylist(result, socketURL);
            }
        };
        task.execute();
    }
}