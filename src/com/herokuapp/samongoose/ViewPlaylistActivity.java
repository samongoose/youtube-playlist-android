package com.herokuapp.samongoose;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import io.socket.*;

public class ViewPlaylistActivity extends Activity {
    public final static String PLAYLIST_URL = "com.herokuapp.samongoose.PLAYLIST_URL";
    public final static String SOCKET_URL = "com.herokuapp.samongoose.SOCKET_URL";
    private String URL;
    SocketIO ioClient = null;
    PlaylistAdapter adapter = null;
    
    @Override
    public void onPause() {
        super.onPause();
        if (ioClient != null) {
            ioClient.disconnect();
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_playlist);

        URL = getIntent().getStringExtra(PLAYLIST_URL);

        Vector<VideoInfo> items = new Vector<VideoInfo>();
        adapter = new PlaylistAdapter(this, items);
        final ListView playlist = (ListView) findViewById(R.id.playlist_list);
        playlist.setAdapter(adapter);

        fetchItems();
        
        String socketURL = getIntent().getStringExtra(SOCKET_URL);
        try {
            ioClient = new SocketIO(socketURL);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ioClient.connect(new IOCallback() {
            @Override
            public void on(String arg0, IOAcknowledge arg1, Object... arg2) {
            }

            @Override
            public void onConnect() {
            }

            @Override
            public void onDisconnect() {
            }

            @Override
            public void onError(SocketIOException arg0) {
                System.out.println("unexpected");
            }

            @Override
            public void onMessage(String arg0, IOAcknowledge arg1) {
                try {
                    JSONObject jsonMsg = new JSONObject(arg0);
                    if (jsonMsg.getString("type").equals("item")) {
                        playlist.post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.clear();
                                fetchItems();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onMessage(JSONObject arg0, IOAcknowledge arg1) {
                try {
                    if (arg0.getString("type").equals("item")) {
                        adapter.clear();
                        fetchItems();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
 
    protected void fetchItems() {
        final ViewPlaylistActivity backPtr = this;
        
        AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String...url) {
                HttpClient client = new DefaultHttpClient();
                HttpGet req = new HttpGet(url[0] + "?fmt=json");
                HttpResponse resp = null;
                try {
                    resp = client.execute(req);
                    if (resp.getStatusLine().getStatusCode() != 200) {
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    return EntityUtils.toString(resp.getEntity());
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
            public void onPostExecute(String resp) {
                if (resp != null) {
                    try {
                        JSONObject jsonResp = new JSONObject(resp);
                        JSONArray vids = jsonResp.getJSONArray("items");
                        for (int i = 0; i < vids.length(); i++) {
                            JSONObject vid = vids.getJSONObject(i);
                            adapter.add(new VideoInfo(vid));
                        }
                        backPtr.setTitle(jsonResp.getString("name"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        task.execute(URL);
    }
    
    final class PlaylistAdapter extends ArrayAdapter<VideoInfo> {
        private LayoutInflater mInflater;
        
        public PlaylistAdapter(Context context, List<VideoInfo> objects) {
            super(context, 0, objects);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //widgets displayed by each item in your list
            TextView item = null;

            //data from your adapter
            VideoInfo rowData= getItem(position);


            //we want to reuse already constructed row views...
            if(null == convertView){
                convertView = mInflater.inflate(R.layout.playlist_item, null);
            }
            item = (TextView) convertView.findViewById(R.id.title);
            item.setText(rowData.Title);

            return convertView;
        }
    }
}
