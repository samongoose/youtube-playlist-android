package com.herokuapp.samongoose;

import org.json.*;

public class VideoInfo {
    public String Title;
    
    public VideoInfo(JSONObject json)
    {
        try {
            Title = json.getJSONObject("title").getString("$t");
        } catch (Exception e)
        {
            Title = "N/A";
        }

    }
}
