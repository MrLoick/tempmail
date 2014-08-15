package com.unlogicon.tempmail.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.AQUtility;
import com.unlogicon.tempmail.R;
import com.unlogicon.tempmail.Settings;
import com.unlogicon.tempmail.Utils;
import com.unlogicon.tempmail.UtilsApi;
import com.unlogicon.tempmail.adapter.MessageAdapter;
import com.unlogicon.tempmail.model.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.unlogicon.tempmail.UtilsApi.getCallBack;

public class ActivityMain extends ActionBarActivity {
    /**
     * Called when the activity is first created.
     */

    private ListView messageList;
    private MessageAdapter messageAdapter;
    public ArrayList<Message> rows;
    private AQuery aq;
    private Menu menu;
    private MenuItem refreshItem;
    private Settings settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        settings = new Settings(this);

        aq = new AQuery(this);
        rows = new ArrayList<Message>();

        messageList = (ListView) findViewById(R.id.messageList);
        messageAdapter = new MessageAdapter(this, rows);
        Utils.addMenuButton(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        refreshItem = menu.findItem(R.id.action_refresh);

        if (settings.getHash() == null) {
            settings.setEmail(Utils.generateRandomEmail(this));
            settings.setHash(Utils.computeMD5(settings.getEmail()));
            getSupportActionBar().setSubtitle(settings.getEmail());
        }
        else {
            getSupportActionBar().setSubtitle(settings.getEmail());
        }

        reloadList();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                reloadList();
                return true;
            case R.id.action_copy:
                Utils.copyToClipboard(this, settings.getEmail());
                return true;
            case R.id.action_about:
                Utils.showAboutDialog(this);
                break;
            case R.id.action_change:
                rows.clear();
                messageAdapter.notifyDataSetChanged();
                settings.setEmail(Utils.generateRandomEmail(this));
                settings.setHash(Utils.computeMD5(settings.getEmail()));
                getSupportActionBar().setSubtitle(settings.getEmail());
                reloadList();
                break;
            case R.id.action_share:
                Utils.shareEmail(this, settings.getEmail());
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void reloadList() {
            updateRefreshState(true);
            aq.progress(this).ajax(getCallBack(this, settings.getHash()));
        }



    public void onRefresh(String url, String s, AjaxStatus status) {
        rows.clear();
        if (s != null) {
            messageAdapter.notifyDataSetInvalidated();
            try {
                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject c = jsonArray.getJSONObject(i);
                    rows.add(UtilsApi.parseMessage(c));
                }
            } catch (Exception e) {
                AQUtility.debug("error List parser", e.toString());
                e.printStackTrace();
            }
            messageAdapter.notifyDataSetChanged();
            messageList.setAdapter(messageAdapter);

        } else {
            AQUtility.debug("error List parser", s);
            aq.ajaxCancel();
        }
        updateRefreshState(false);
    }

    private void updateRefreshState(boolean refresh) {

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.action_refresh, null);
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh);
        rotation.setRepeatCount(Animation.INFINITE);
        if (refresh) {
            iv.startAnimation(rotation);
            refreshItem.setActionView(iv);
        } else {
            refreshItem.getActionView().clearAnimation();
            refreshItem.setActionView(null);
        }

    }

}



