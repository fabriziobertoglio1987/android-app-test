package com.example.mobileapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.support.v7.widget.Toolbar;

import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksAdapter;
import com.basecamp.turbolinks.TurbolinksView;

public class MainActivity extends AppCompatActivity implements TurbolinksAdapter {
    // Change the BASE_URL to an address that your VM or device can hit.
    private static final String BASE_URL = "http://192.168.1.52:3000";
    // private static final String BASE_URL = "https://sprachspiel.xyz";
    private static final String INTENT_URL = "intentUrl";

    private String location;
    private TurbolinksView turbolinksView;
    private Menu mMenu;

    // -----------------------------------------------------------------------
    // Activity overrides
    // -----------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the custom TurbolinksView object in your layout
        turbolinksView = (TurbolinksView) findViewById(R.id.turbolinks_view);

        // Setting http user agent of the request performed to the server with the android app. so that logics on the back end can be written for the android app
        // https://stackoverflow.com/questions/26778882/check-if-a-request-came-from-android-app-in-rails
        // https://stackoverflow.com/questions/5586197/android-user-agent#5590105
        // https://github.com/ruby-china/ruby-china-android/blob/master/app/src/main/java/org/ruby_china/android/MainActivity.java#L70

        WebSettings webSettings = TurbolinksSession.getDefault(this).getWebView().getSettings();
        webSettings.setUserAgentString("turbolinks-app, sprachspiel, official, android");

        // Creating a native navigation toolbar as described in https://speakerdeck.com/tamcgoey/building-hybrid-apps-with-rails-a-case-study
        // https://github.com/tamcgoey/dasher-app-android/blob/master/src/main/java/com/usedashnow/dasher/ActiveDashActivity.java
        Toolbar mToolbar = (Toolbar) findViewById(R.id.activity_toolbar);
        mToolbar.setTitle(R.string.title_activity);
        mToolbar.setTitleTextColor(Color.WHITE);
        int padding = 0;
        mToolbar.setPadding(padding, mToolbar.getPaddingTop(), mToolbar.getPaddingRight(), mToolbar.getPaddingBottom());
        int zero = 0;
        mToolbar.setTitleMarginStart(zero);
        mToolbar.setTitleMarginEnd(zero);
        mToolbar.setContentInsetsAbsolute(zero, zero);
        setSupportActionBar(mToolbar);

        // I add the Javascript interface from WebService.java showToast()
        // https://github.com/tamcgoey/dasher-app-android/blob/master/src/main/java/com/usedashnow/dasher/ActiveDashActivity.java and to set the message read https://speakerdeck.com/tamcgoey/building-hybrid-apps-with-rails-a-case-study
        TurbolinksSession.getDefault(this).addJavascriptInterface(new WebService(this), "Android");

        // Running Javascript to hide the navbar, it will not give back any result except hiding the navbar
        // https://github.com/ruby-china/ruby-china-android/blob/master/app/src/main/java/org/ruby_china/android/MainActivity.java
        //TurbolinksSession.getDefault(this).getWebView().evaluateJavascript(
        //        "$('.navbar').hide();",
        //        null
        //);

        // For this demo app, we force debug logging on. You will only want to do
        // this for debug builds of your app (it is off by default)
        TurbolinksSession.getDefault(this).setDebugLoggingEnabled(true);

        // For this example we set a default location, unless one is passed in through an intent
        location = getIntent().getStringExtra(INTENT_URL) != null ? getIntent().getStringExtra(INTENT_URL) : BASE_URL;

        // Execute the visit
        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .view(turbolinksView)
                .visit(location);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // Since the webView is shared between activities, we need to tell Turbolinks
        // to load the location from the previous activity upon restarting
        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .restoreWithCachedSnapshot(true)
                .view(turbolinksView)
                .visit(location);
    }

    // -----------------------------------------------------------------------
    // TurbolinksAdapter interface
    // -----------------------------------------------------------------------

    @Override
    public void onPageFinished() {

    }

    @Override
    public void onReceivedError(int errorCode) {
        handleError(errorCode);
    }

    @Override
    public void pageInvalidated() {

    }

    @Override
    public void requestFailedWithStatusCode(int statusCode) {
        handleError(statusCode);
    }

    @Override
    public void visitCompleted() {

    }

    // The starting point for any href clicked inside a Turbolinks enabled site. In a simple case
    // you can just open another activity, or in more complex cases, this would be a good spot for
    // routing logic to take you to the right place within your app.
    @Override
    public void visitProposedToLocationWithAction(String location, String action) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(INTENT_URL, location);

        this.startActivity(intent);
    }

    // overriding the onCreateOptionsMenu to inflate the menu.xml icons in the Toolbar
    // the icons are res/menu/account.xml and this function sets the icons for this activity/screen
    // https://github.com/tamcgoey/dasher-app-android/blob/master/src/main/java/com/usedashnow/dasher/ActiveDashActivity.java

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.account, menu);

        this.mMenu = menu;

        return true;
    }

    // Handles the select of the above inflated icons
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_profile:
                // in this case i send the user to https://sprachspiel.xyz/users/edit
                // the home page for editing his user profile
                // Intent intent = new Intent(this, ProfileActivity.class);
                // intent.putExtra(INTENT_URL, BASE_URL + "/users/edit" );
                visitProposedToLocationWithAction(BASE_URL + "/users/edit", "advance");
                //this.startActivity(intent);
                return true;
            case R.id.action_help:
                // in this other case I send him to his room, now I hardcoded room 11
                // but I need to find a way to get this parameter from the backend
                // or from the android webview
                Intent intentRoom = new Intent(this, RoomActivity.class);
                intentRoom.putExtra(INTENT_URL, BASE_URL + "/rooms/11");
                this.startActivity(intentRoom);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    // Simply forwards to an error page, but you could alternatively show your own native screen
    // or do whatever other kind of error handling you want.
    private void handleError(int code) {
        if (code == 404) {
            TurbolinksSession.getDefault(this)
                    .activity(this)
                    .adapter(this)
                    .restoreWithCachedSnapshot(false)
                    .view(turbolinksView)
                    .visit(BASE_URL + "/error");
        }
    }
}