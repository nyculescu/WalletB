package layout.main.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import nyc.walletb.R;

public class SettingsFragment extends FragmentComplement implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {
    private final String TAG = getClass().getName();

    // Google sign in suite
    private static final int google_RC_SIGN_IN = 9001;
    private GoogleApiClient google_ApiClient;
    private ProgressDialog google_ProgressDialog;
    private SignInButton google_SignInButton;
    private LinearLayout google_SigInLayout;
    private TextView google_SignInStatus;
    private static String google_SignInName = null;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        super.init(view, R.id.fragment_settings_framelayout);

        google_SignInStatus = (TextView) view.findViewById(R.id.frg_settings_status_txtv);

        google_SigInLayout = (LinearLayout) view.findViewById(R.id.sign_out_and_disconnect);

        // Button listeners
        Button signout_button = (Button) view.findViewById(R.id.sign_out_button);
        signout_button.setOnClickListener(this);
        Button disconnect_button = (Button) view.findViewById(R.id.disconnect_button);
        disconnect_button.setOnClickListener(this);
        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        google_ApiClient = new GoogleApiClient.Builder(view.getContext())
                .enableAutoManage(this.getActivity() /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END build_client]

        // [START customize_button]
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        google_SignInButton = (SignInButton) view.findViewById(R.id.sign_in_button);
        google_SignInButton.setOnClickListener(this);
        google_SignInButton.setSize(SignInButton.SIZE_STANDARD);
        google_SignInButton.setScopes(gso.getScopeArray());
        // [END customize_button]

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(google_ApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in");
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }

        return view;
    }

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            assert acct != null;
            Log.d(TAG, "handleSignInResult | " + getString(R.string.signed_in_fmt, acct.getDisplayName()));
            google_SignInName = getString(R.string.signed_in_fmt, acct.getDisplayName());
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            Log.d(TAG, "handleSignInResult | " + "Signed out");
            updateUI(false);
        }
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
        Log.d(TAG,"signIn()");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(google_ApiClient);
        startActivityForResult(signInIntent, google_RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        Log.d(TAG,"signOut()");
        Auth.GoogleSignInApi.signOut(google_ApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(google_ApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        // [START_EXCLUDE]
                        updateUI(false);
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.e(TAG, "onConnectionFailed: " + connectionResult);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (google_ProgressDialog != null) {
            google_ProgressDialog.dismiss();
        }
    }

    private void showProgressDialog() {
        if (google_ProgressDialog == null) {
            google_ProgressDialog = new ProgressDialog(this.getContext());
            google_ProgressDialog.setMessage(getString(R.string.loading));
            google_ProgressDialog.setIndeterminate(true);
        }

        google_ProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (google_ProgressDialog != null && google_ProgressDialog.isShowing()) {
            google_ProgressDialog.hide();
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            try {
                google_SignInButton.setVisibility(View.GONE);
                google_SigInLayout.setVisibility(View.VISIBLE);

                try {
                    google_SignInStatus.setText(google_SignInName);
                }catch (NullPointerException e){
                    Log.e(TAG, "updateUI | failed, because google_SignInName is null");
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "updateUI | failed to update the UI");
            }
        } else {
            //google_SignInStatus.setText(R.string.signed_out);
            Log.d(TAG, "updateUI | User is signed out");
            google_SignInStatus.setText(R.string.signed_out);

            google_SignInButton.setVisibility(View.VISIBLE);
            google_SigInLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.disconnect_button:
                revokeAccess();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == google_RC_SIGN_IN) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                updateUI(true);
            }
        }
    }
}
