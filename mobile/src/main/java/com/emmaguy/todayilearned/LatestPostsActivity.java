package com.emmaguy.todayilearned;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

import com.emmaguy.todayilearned.sharedlib.Post;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LatestPostsActivity extends ListActivity implements AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_latest_posts);

        Gson gson = new Gson();
        ArrayList<Post> latestPosts = gson.fromJson(getSharedPreferences().getString(SettingsActivity.PREFS_REDDIT_POSTS, null), new TypeToken<ArrayList<Post>>() {}.getType());

        List<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();

        for(Post p : latestPosts) {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("title", p.getTitle());
            hashMap.put("post", p.getSubreddit());
            hashMap.put("permalink", p.getPermalink());
            items.add(hashMap);
        }

        getListView().setAdapter(new SimpleAdapter(this, items, R.layout.row_latest_posts, new String[]{"title", "post"}, new int[]{android.R.id.text1, android.R.id.text2}));
        getListView().setOnItemClickListener(this);
    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.latest_posts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        HashMap<String, String> item = (HashMap<String, String>) adapterView.getAdapter().getItem(i);
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.reddit.com" + item.get("permalink"))));
    }
}
