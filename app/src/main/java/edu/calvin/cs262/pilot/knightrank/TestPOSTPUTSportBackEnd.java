package edu.calvin.cs262.pilot.knightrank;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Objects;

public class TestPOSTSportBackEnd extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    // Private class members.
    private static final String LOG_TAG = TestPOSTSportBackEnd.class.getSimpleName();

    private EditText editTextViewSportName;
    private EditText editTextViewSportType;
    private TextView textViewNetworkStatus;
    private TextView textViewRequestStatus;

    private String sportNameString;
    private String sportTypeString;

    // Share preferences file (custom)
    private SharedPreferences mPreferences;
    // Shared preferences file (default)
    private SharedPreferences mPreferencesDefault;

    // Name of the custom shared preferences file.
    private static final String sharedPrefFile = "pilot.cs262.calvin.edu.knightrank";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_postsport_back_end);

        // my_child_toolbar is defined in the layout file
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // Set shared preferences component.
        mPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        mPreferencesDefault = PreferenceManager.getDefaultSharedPreferences(this);

        // Placeholder code as example of how to get values from the default SharedPrefs file.
        String syncFreq = mPreferencesDefault.getString(SettingsActivity.KEY_SYNC_FREQUENCY, "-1");

        // Find components.
        editTextViewSportName = findViewById(R.id.input_sport_name);
        editTextViewSportType = findViewById(R.id.input_sport_type);
        textViewNetworkStatus = findViewById(R.id.network_connected);
        textViewRequestStatus = findViewById(R.id.request_status);

        // Reconnect to the loader if one exists already, upon device config change.
        if (getSupportLoaderManager().getLoader(0) != null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }

        // Change the background color to what was selected in color picker.
        // Note: Change color by using findViewById and ID of the UI element you wish to change.
        RelativeLayout thisLayout = findViewById(R.id.activity_test_sport_post_back_end_root_layout);
        thisLayout.setBackgroundColor(mPreferences.getInt(ColorPicker.APP_BACKGROUND_COLOR_ARGB, Color.YELLOW));

        int value = mPreferences.getInt(ColorPicker.APP_BACKGROUND_COLOR_ARGB, Color.BLACK);

        int toolbarColor = mPreferences.getInt(ColorPicker.APP_TOOLBAR_COLOR_ARGB, Color.YELLOW);

        // Change the toolbar color to what was selected in color picker.
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(toolbarColor));

        Log.e(LOG_TAG, "Value of color is: " + value);
    }

    /**
     * Method begins Google Cloud endpoint put process upon button click.
     *
     * @param view view component
     */
    public void put(View view) {
    }

    /**
     * Method begins Google Cloud endpoint post process upon button click.
     *
     * @param view view component
     */
    public void post(View view) {

        // Get the sport input strings.
        sportNameString = editTextViewSportName.getText().toString();
        sportTypeString = editTextViewSportType.getText().toString();

        if (sportNameString.length() == 0) {
            sportNameString = "default sport name";
        }
        if (sportTypeString.length() == 0) {
            sportTypeString = "default sport type";
        }

        // Close keyboard after hitting search query button.
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

        // Initialize network info components.
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = null;

        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        // Check connection is available, we are connected.
        if (networkInfo != null && networkInfo.isConnected()) {

            // show "Connected" & type of network "WIFI or MOBILE"
            textViewNetworkStatus.setText("Connected " + networkInfo.getTypeName());
            // change background color to red
            textViewNetworkStatus.setBackgroundColor(0xFF7CCC26);

            // Refactored to user AsyncTaskLoader via PlayerGETLoader.java
            Bundle postBundle = new Bundle();
            postBundle.putString("sport_name", sportNameString);
            postBundle.putString("sport_type", sportTypeString);
            getSupportLoaderManager().restartLoader(0, postBundle, this);

            // Indicate to user query is in process.
            textViewRequestStatus.setText("");
            textViewRequestStatus.setText("POST in-progress");
        } else {

            // show "Not Connected"
            textViewNetworkStatus.setText("Not Connected");
            // change background color to green
            textViewNetworkStatus.setBackgroundColor(0xFFFF0000);

            // There is no available connection.
            textViewRequestStatus.setText("");
            textViewRequestStatus.setText(R.string.no_connection);
        }
    }

    /**
     * Method adds an options menu to the toolbar.
     *
     * @param menu menu object
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Method to control what happens when menu items are selected.
     *
     * @param item the item selected
     * @return whatever
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent1 = new Intent(this, SettingsActivity.class);
                startActivity(intent1);
                return true;
            case R.id.color_picker:
                Intent intent2 = new Intent(this, ColorPicker.class);
                startActivity(intent2);
                return true;
            default:
                // Do nothing
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method called when load is instantiated.
     * <p>
     * NOTE: change the Loader.java class if we want to obtain different data.
     *
     * @param id
     * @param bundle contains data
     * @return query results
     */
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle bundle) {
        return new SportPOSTLoader(this, bundle.getString("sport_name"), bundle.getString("sport_type"));
    }

    /**
     * Method called when loader task is finished.  Add code to update UI with results.
     * <p>
     * NOTE: modify as necessary according to the data we obtained.
     *
     * @param loader loader object.
     * @param s
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String s) {

        // Method call to test match GET.
        TestSportPOSTBackend(s);
    }

    /**
     * Method to test whether we can successfully post to the database.
     *
     * @param s response message from the RESTful web service.
     */
    private void TestSportPOSTBackend(String s) {

        // POST the response we get to the TextView.
        textViewRequestStatus.setText("Response from RESTful web service\n");
        textViewRequestStatus.append("OK = success, anything else = BAD\n");
        textViewRequestStatus.append("Result:");
        textViewRequestStatus.append(s);
    }

    /**
     * Method called to clean up any remaining resources.
     *
     * @param loader loader object.
     */
    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }
}