package nyc.walletb;

import android.animation.Animator;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.aurelhubert.ahbottomnavigation.notification.AHNotification;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * The UI was inherited from https://github.com/aurelhubert/ahbottomnavigation
 */
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    final String TAG = getClass().getName();

    // Google Spreadsheet
    GoogleAccountCredential spreadsheet_credential;
    ProgressDialog spreadsheet_progressBar;
    static final int spreadsheet_REQUEST_ACCOUNT_PICKER = 1000;
    static final int spreadsheet_REQUEST_AUTHORIZATION = 1001;
    static final int spreadsheet_REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int spreadsheet_REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    static final String spreadsheet_PREF_ACCOUNT_NAME = "accountName";
    // com.google.api.client.googleapis.json.GoogleJsonResponseException: 403 Forbidden  is caused by  SheetsScopes.SPREADSHEETS_READONLY
    private static final String[] spreadsheet_SCOPES = {SheetsScopes.SPREADSHEETS};
    private static final String spreadsheet_ID = "1jwMOrr9fcjIkw3DM-yTBhw8UbvICE6P9UQ_bFI7KWpQ";
    private static final String spreadsheet_sheetID = "Sheet1";

    private MainFragment currentFragment;
    private MainViewPagerAdapter adapter;
    //private AHBottomNavigationAdapter navigationAdapter;
    private ArrayList<AHBottomNavigationItem> bottomNavigationItems = new ArrayList<>();
    //private boolean useMenuResource = true;
    //private int[] tabColors;
    private Handler handler = new Handler();

    // UI
    private AHBottomNavigationViewPager viewPager;
    private AHBottomNavigation bottomNavigation;
    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_home);
        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * Init UI
     */
    private void initUI() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        viewPager = (AHBottomNavigationViewPager) findViewById(R.id.view_pager);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);

        // bottomNavigation.setNotification("1", 3);
        bottomNavigationItems.add(new AHBottomNavigationItem(R.string.tab_1, R.drawable.ic_apps_black_24dp, R.color.color_tab_1));
        bottomNavigationItems.add(new AHBottomNavigationItem(R.string.tab_2, R.drawable.icon_bill, R.color.color_tab_2));
        bottomNavigationItems.add(new AHBottomNavigationItem(R.string.tab_3, R.drawable.ic_maps_local_restaurant, R.color.color_tab_3));
        bottomNavigationItems.add(new AHBottomNavigationItem(R.string.tab_4, R.drawable.ic_maps_local_bar, R.color.color_tab_4));
        bottomNavigationItems.add(new AHBottomNavigationItem(R.string.tab_5, R.drawable.ic_maps_place, R.color.color_tab_5));
        bottomNavigation.addItems(bottomNavigationItems);

        bottomNavigation.manageFloatingActionButtonBehavior(floatingActionButton);
        bottomNavigation.setTranslucentNavigationEnabled(true);

        bottomNavigation.setColored(true);

        bottomNavigation.restoreBottomNavigation(true);

        bottomNavigation.setSelectedBackgroundVisible(true);

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {

                if (currentFragment == null) {
                    currentFragment = adapter.getCurrentFragment();
                }

                if (wasSelected) {
                    currentFragment.refresh();
                    return true;
                }

                if (currentFragment != null) {
                    currentFragment.willBeHidden();
                }

                viewPager.setCurrentItem(position, false);

                if (currentFragment == null) {
                    return true;
                }

                currentFragment = adapter.getCurrentFragment();
                currentFragment.willBeDisplayed();

                if (position == 1) {
                    bottomNavigation.setNotification("", 1);

                    floatingActionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });

                    floatingActionButton.setVisibility(View.VISIBLE);
                    floatingActionButton.setImageResource(android.R.drawable.ic_menu_camera);
                    floatingActionButton.setAlpha(0f);
                    floatingActionButton.setScaleX(0f);
                    floatingActionButton.setScaleY(0f);
                    floatingActionButton.animate()
                            .alpha(1)
                            .scaleX(1)
                            .scaleY(1)
                            .setDuration(300)
                            .setInterpolator(new OvershootInterpolator())
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    floatingActionButton.animate()
                                            .setInterpolator(new LinearOutSlowInInterpolator())
                                            .start();
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            })
                            .start();

                } else {
                    if (floatingActionButton.getVisibility() == View.VISIBLE) {
                        floatingActionButton.animate()
                                .alpha(0)
                                .scaleX(0)
                                .scaleY(0)
                                .setDuration(300)
                                .setInterpolator(new LinearOutSlowInInterpolator())
                                .setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        floatingActionButton.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                        floatingActionButton.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                })
                                .start();
                    }
                }

                return true;
            }
        });

        viewPager.setOffscreenPageLimit(4);
        adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        currentFragment = adapter.getCurrentFragment();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Setting custom colors for notification
                AHNotification notification = new AHNotification.Builder()
                        .setText(":)")
                        .setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.color_notification_back))
                        .setTextColor(ContextCompat.getColor(MainActivity.this, R.color.color_notification_text))
                        .build();
                bottomNavigation.setNotification(notification, 1);
                Snackbar.make(bottomNavigation, "Snackbar with bottom navigation",
                        Snackbar.LENGTH_SHORT).show();

            }
        }, 2000);

        //bottomNavigation.setDefaultBackgroundResource(R.drawable.bottom_navigation_background);

        spreadsheet_progressBar = new ProgressDialog(this);
        spreadsheet_progressBar.setMessage("Calling Google Sheets API ...");
        // Initialize credentials and service object.
        spreadsheet_credential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(spreadsheet_SCOPES))
                .setBackOff(new ExponentialBackOff());
        getResultsFromApi();
    }

    /**
     * Return if the bottom navigation is colored
     */
    public boolean isBottomNavigationColored() {
        return bottomNavigation.isColored();
    }

    /**
     * Reload activity
     */
    public void reload() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Return the number of items in the bottom navigation
     */
    public int getBottomNavigationNbItems() {
        return bottomNavigation.getItemsCount();
    }


    /********************** implementation for EasyPermissions.PermissionCallbacks **********************/
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            Log.d(TAG, "getResultsFromApi | acquireGooglePlayServices");
            acquireGooglePlayServices();
        } else if (spreadsheet_credential.getSelectedAccountName() == null) {
            chooseAccount();
            Log.d(TAG, "getResultsFromApi | chooseAccount");
        } else if (!isDeviceOnline()) {
            Toast.makeText(this, "No network connection available.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "getResultsFromApi | No network connection available.");
        } else {
            Log.d(TAG, "getResultsFromApi | MakeRequestTask with the chosen credential");
            new MakeRequestTask(spreadsheet_credential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(spreadsheet_REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = getPreferences(Context.MODE_PRIVATE).getString(spreadsheet_PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                Log.d(TAG, "chooseAccount | getResultsFromApi");
                spreadsheet_credential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                Log.d(TAG, "chooseAccount | Start a dialog from which the user can choose an account");
                // Start a dialog from which the user can choose an account
                startActivityForResult(spreadsheet_credential.newChooseAccountIntent(), spreadsheet_REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(this, "This app needs to access your Google account (via Contacts).",
                    spreadsheet_REQUEST_PERMISSION_GET_ACCOUNTS, Manifest.permission.GET_ACCOUNTS);
            Log.d(TAG, "chooseAccount | Request the GET_ACCOUNTS permission via a user dialog");
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case spreadsheet_REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Log.e(TAG, "onActivityResult | This app requires Google Play Services. Please install Google Play Services on your device and relaunch this app.");
                    Toast.makeText(this, "This app requires Google Play Services. Please install " +
                            "Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "onActivityResult | spreadsheet_REQUEST_GOOGLE_PLAY_SERVICES | getResultsFromApi()");
                    getResultsFromApi();
                }
                break;
            case spreadsheet_REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        Log.d(TAG, "onActivityResult" + "spreadsheet_REQUEST_ACCOUNT_PICKER | getResultsFromApi()");
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(spreadsheet_PREF_ACCOUNT_NAME, accountName).apply();
                        spreadsheet_credential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case spreadsheet_REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "onActivityResult" + "spreadsheet_REQUEST_AUTHORIZATION | getResultsFromApi()");
                    getResultsFromApi();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        Log.d(TAG, "isDeviceOnline");
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        Log.d(TAG, "isGooglePlayServicesAvailable");
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        Log.d(TAG, "acquireGooglePlayServices");
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            Log.d(TAG, "acquireGooglePlayServices | showGooglePlayServicesAvailabilityErrorDialog");
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        Log.d(TAG, "showGooglePlayServicesAvailabilityErrorDialog");
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(MainActivity.this, connectionStatusCode, spreadsheet_REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        final String TAG = getClass().getName();

        private com.google.api.services.sheets.v4.Sheets mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            Log.d(TAG, "MakeRequestTask");
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.sheets.v4.Sheets.Builder(transport, jsonFactory, credential).setApplicationName("Google Sheets API Android Quickstart").build();
        }

        /**
         * Background task to call Google Sheets API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                Log.d(TAG, "doInBackground | getDataFromGoogleSheets()");
                return getDataFromGoogleSheets();
            } catch (Exception e) {
                Log.e(TAG, "doInBackground | " + e);
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * SAMPLE ********************** https://developers.google.com/sheets/api/quickstart/android
         * Fetch a list of names and majors of students in a sample spreadsheet (tested from an internal sheet):
         * <p>
         * from google samples: https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
         * <p>
         * (configs: Anyone who has the link can view) todo: the config should be done from the app
         * https://docs.google.com/spreadsheets/d/1jwMOrr9fcjIkw3DM-yTBhw8UbvICE6P9UQ_bFI7KWpQ/edit#gid=0
         *
         * @return List of names and majors
         * @throws IOException
         */
        private List<String> getDataFromGoogleSheets() throws IOException {
            String range = spreadsheet_sheetID + "!A2:E"; // <name of the sheet>!<from cell>:<to cell>
            List<String> results = new ArrayList<>();
            ValueRange response = this.mService.spreadsheets().values()
                    .get(spreadsheet_ID, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values != null) {
                Log.d(TAG, "getDataFromGoogleSheets | get values from the spreadsheet");
                results.add("Name, Major");
                for (List row : values) {
                    /*
                    Name	    Gender	Class Level	Home State	Major	    Extracurricular Activity
                    Gogosel	    Male	3. Middle	RO	        Automotive	Android
                    Gogosica	Female	2. Junior	RO	        Automotive	Food
                    * */
                    results.add(row.get(0) + ", " + row.get(4));
                }
            } else {
                Log.d(TAG, "getDataFromGoogleSheets | error on getting values from the spreadsheet");
            }
            return results;
        }

        /**
         * Append data after a table of data in a sheet. Reference  https://developers.google.com/sheets/api/guides/values
         */
        private void appendDataToGoogleSheets(List<List<String>> cellValues) {
            //TODO: for now, this method of casting List<List<String>> to List<List<Object>> is done. To be redefined
            List<List<Object>> listToBeWritten = new ArrayList<>();
            if (cellValues != null)
                for (int i = 0; i < cellValues.size(); i++) {
                    if (cellValues.get(i) != null)
                        listToBeWritten.add(new ArrayList<Object>(cellValues.get(i)));
                }

            final ValueRange body = new ValueRange().setValues(listToBeWritten);
            final String range = spreadsheet_sheetID + "!A3:E";
            final com.google.api.services.sheets.v4.Sheets service = this.mService;

            AsyncTask<Void, Void, AppendValuesResponse> appendDataToGoogleSheets_task = new AsyncTask<Void, Void, AppendValuesResponse>() {
                @Override
                protected AppendValuesResponse doInBackground(Void... params) {
                    AppendValuesResponse result = null;
                    try {
                        result = service.spreadsheets().values().append(spreadsheet_ID, range, body)
                                //Google Sheets API v4 and valueInputOption  https://stackoverflow.com/questions/37785216/google-sheets-api-v4-and-valueinputoption
                                .setValueInputOption("USER_ENTERED") //https://developers.google.com/sheets/api/reference/rest/v4/ValueInputOption
                                .execute();
                    } catch (IOException exception) {
                        Log.e(TAG, "appendDataToGoogleSheets | " + exception.toString());
                    }
                    return result;
                }

                @Override
                protected void onPostExecute(AppendValuesResponse resultFromTheAppendingTask) {
                    Log.d(TAG, "appendDataToGoogleSheets | " + resultFromTheAppendingTask);
                }
            };
            appendDataToGoogleSheets_task.execute();
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute");
            spreadsheet_progressBar.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            spreadsheet_progressBar.hide();
            if (output == null || output.size() == 0) {
                Log.e(TAG, "onPostExecute | No results returned.");
                Toast.makeText(getBaseContext(), "No results returned.", Toast.LENGTH_LONG).show();
            } else {
                //output.add(0, "Data retrieved using the Google Sheets API:");
                Log.d(TAG, "onPostExecute | Data retrieved using the Google Sheets API:" + output);
                Toast.makeText(getBaseContext(), TextUtils.join("\n", output), Toast.LENGTH_LONG).show();

                //TODO: 9/25/2017  Used for debug. Delete it after successful writing into the document
                // append just for row 0 and row 4
                appendDataToGoogleSheets(Arrays.asList(
                        Arrays.asList("Catalin", "", "", "", "System Engineer"),
                        Arrays.asList("Alisa", "", "", "", "Programmer")
                ));
            }
        }

        @Override
        protected void onCancelled() {
            spreadsheet_progressBar.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    Log.d(TAG, "onCancelled | showGooglePlayServicesAvailabilityErrorDialog()");
                    showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) mLastError).getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    Log.d(TAG, "onCancelled | startActivityForResult()");
                    startActivityForResult(((UserRecoverableAuthIOException) mLastError).getIntent(), MainActivity.spreadsheet_REQUEST_AUTHORIZATION);
                } else {
                    Log.e(TAG, "onCancelled | The following error occurred:\n" + mLastError.getMessage());
                    Toast.makeText(getBaseContext(), "The following error occurred:\n" + mLastError.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "onCancelled | Request cancelled.");
                Toast.makeText(getBaseContext(), "Request cancelled.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
