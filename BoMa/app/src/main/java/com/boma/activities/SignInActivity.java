package com.boma.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.boma.R;
import com.boma.utils.CustomLog;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class SignInActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private String LOG_TAG ="SignInActivity";
    private GoogleApiClient mGoogleApiClient;
    int FB_SIGN_IN = 5;
    int G_SIGN_IN = 6;
    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;
    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;
    CallbackManager mFbCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initGooglePlusLogin();

        try {
            initFacebookLogin();
        } catch (Exception e) {
            CustomLog.error(LOG_TAG, "FBException::" + e.getMessage());
        }

    }

    private void initFacebookLogin() {
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        mFbCallbackManager = CallbackManager.Factory.create();
        ImageView lFbLoginButton = (ImageView) findViewById(R.id.fb_login_button);
        lFbLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(SignInActivity.this, Arrays.asList("public_profile", "email", "user_friends"));
            }
        });
        LoginManager.getInstance().registerCallback(mFbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Toast.makeText(SignInActivity.this, "FbLoginSuccessfull", Toast.LENGTH_LONG).show();

                String accessToken = loginResult.getAccessToken().getToken();
                CustomLog.info(LOG_TAG, "doFacebookLogin: onSuccess Token is " +
                        accessToken);
                try {
                    CustomLog.info(LOG_TAG, "doFacebookLogin: onSuccess Permissions are "
                            +
                            AccessToken.getCurrentAccessToken().getPermissions().toString());
                } catch (Exception e) {

                }
                Bundle b = new Bundle();
                b.putString("fields", "id,first_name,email,gender,last_name,age_range,friends");
                new GraphRequest(AccessToken.getCurrentAccessToken(), "/v2.4/me", b, HttpMethod.GET,
                        new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                JSONObject responseJson = response.getJSONObject();
                                if (responseJson != null) {
                                    String lSocialId = responseJson.optString("id");
                                    CustomLog.info("FaceboookLoginresponse", responseJson.toString());
                                    String lBirthday = responseJson.optString("birthday") != null ? responseJson.optString("birthday") : "";
                                    String lLocation = responseJson.optString("location") != null ? responseJson.optString("location") : "";
                                    String lRelationShipStatus = responseJson.optString("relationship_status") != null ? responseJson.optString("relationship_status") : "";
                                    String lAgerange = responseJson.optString("age_range") != null ? responseJson.optString("age_range") : "";
                                    String lGender = responseJson.optString("gender") != null ? responseJson.optString("gender") : "";
                                    // Getting FB image
                                    URL imgUrl = null;
                                    try {
                                        imgUrl = new URL("https://graph.facebook.com/" + lSocialId + "/picture?type=large");
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });

    }

    private void initGooglePlusLogin() {

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
//                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API).addScope(new Scope(Scopes.PLUS_LOGIN)).build();

        SignInButton mGplusSignInButton = (SignInButton) findViewById(R.id.google_sign_in_button);
        mGplusSignInButton.setSize(SignInButton.SIZE_STANDARD);
        mGplusSignInButton.setScopes(gso.getScopeArray());
        mGplusSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doGoogleLogin();
            }
        });
    }

    private void doGoogleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, G_SIGN_IN);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(LOG_TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(SignInActivity.this, "Hello:" + acct.getDisplayName(), Toast.LENGTH_LONG).show();
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(SignInActivity.this, "Google SignIn not Successful", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 1) {
            if (resultCode != RESULT_OK) {
                mShouldResolve = false;
            }
            mIsResolving = false;
            mGoogleApiClient.connect();
        }
        mFbCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Do google login.
     */
    private void doGoogleLogin() {
        mShouldResolve = true;
        if (mGoogleApiClient.isConnected()) {
            onConnected(null);
        } else {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
//        LogInEnableAndProgressDisable(false, 2);

        CustomLog.debug(LOG_TAG, "onConnected:" + bundle);
        mShouldResolve = false;
        // Plus.PeopleApi.loadVisible(mGoogleApiClient,
        // null).setResultCallback(this);
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Toast.makeText(SignInActivity.this, "Google SignIn Successful", Toast.LENGTH_LONG).show();

            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
            // String personName = currentPerson.getDisplayName();
            Log.d("GooglePerson", currentPerson.toString());
            String lSocialId = currentPerson.getId();
            String lLocation = currentPerson.getCurrentLocation() != null ? currentPerson.getCurrentLocation() : "";
            Person.Name fullName = currentPerson.getName();
            String firstName = fullName.getGivenName();
            String lastName = fullName.getFamilyName();
            int gender = -1;
            if (currentPerson.hasGender()) // it's not guaranteed
                gender = currentPerson.getGender();

            String GooglePlusGender = "";

            if (gender != -1) {
                if (gender == 0) {
                    GooglePlusGender = "Male";
                } else if (gender == 1) {
                    GooglePlusGender = "Female";
                } else if (gender == 2) {
                    GooglePlusGender = "Others";
                }
            } else {
                GooglePlusGender = "Not Specified";
            }

            String GoogleAgeRangeString = "";

            if (currentPerson.hasAgeRange()) {
                int AgeRange = currentPerson.getAgeRange().getMin();
                GoogleAgeRangeString = String.valueOf(AgeRange);
            }

            String lBirthday = "";

            if (currentPerson.hasBirthday()) {
                lBirthday = currentPerson.getBirthday();
//                new CustomLog().info(LOG_TAG, "Birthday::" + lBirthday);
            }

            if (ContextCompat.checkSelfPermission(SignInActivity.this,
                    android.Manifest.permission.GET_ACCOUNTS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
//                if (ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this,
//                        android.Manifest.permission.GET_ACCOUNTS)) {
//                    showMessageOKCancel("You need to allow access to Google Account",
//                            new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    ActivityCompat.requestPermissions(SignInActivity.this,
//                                            new String[]{android.Manifest.permission.GET_ACCOUNTS},
//                                            0);
//                                }
//                            });
//                    // Show an expanation to the user *asynchronously* -- don't block
//                    // this thread waiting for the user's response! After the user
//                    // sees the explanation, try again to request the permission.
//
//                } else {
//
//                    // No explanation needed, we can request the permission.
//
//                    ActivityCompat.requestPermissions(SignInActivity.this,
//                            new String[]{android.Manifest.permission.GET_ACCOUNTS},
//                            0);
//
//                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                    // app-defined int constant. The callback method gets the
//                    // result of the request.
//                }
            } else {
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                String personPhoto = currentPerson.getImage().getUrl();
                if (personPhoto.contains("=")) {
                    String imageSPlitArray[] = personPhoto.split("=");
                    personPhoto = imageSPlitArray[0] + "=200";
                    System.out.println(personPhoto);
                }
                String personGooglePlusProfile = currentPerson.getUrl();
//                    mSharedPref.putProfileImageUrl(personPhoto);
            /* Getting all category data from server storing it to db. */
//                postGoogleLogin(lSocialId, firstName, lastName, email, GooglePlusGender, GoogleAgeRangeString, personPhoto, lBirthday, lLocation);
            }
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(SignInActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
//        LogInEnableAndProgressDisable(true, 2);
    }

    @Override
    public void onConnectionSuspended(int i) {
//        LogInEnableAndProgressDisable(true, 2);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        CustomLog.error(LOG_TAG, "GooglePlus" + "onConnectionFailed:" + connectionResult);
        if (connectionResult.getErrorCode() == ConnectionResult.SERVICE_MISSING || connectionResult.getErrorCode() == ConnectionResult.SERVICE_DISABLED || connectionResult.getErrorCode() == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            showSnackBar("Please update your Google Play Services");
            return;
        }
//        LogInEnableAndProgressDisable(true, 2);
        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, 1);// RC_SIGN_IN
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e(LOG_TAG, "Could not resolve ConnectionResult.", e);
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
//                LogInEnableAndProgressDisable(false, 2);

            }
        }
    }

    private void setGoogleData() {
//        LogInEnableAndProgressDisable(false, 2);
        String GooglePlusGender = "";
        String GoogleAgeRangeString = "";
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Toast.makeText(SignInActivity.this, "Google SignIn Successfull", Toast.LENGTH_LONG).show();
            Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
//            new CustomLog().info("GooglePerson", currentPerson.toString());
            String lSocialId = currentPerson.getId();
            String lLocation = currentPerson.getCurrentLocation() != null ? currentPerson.getCurrentLocation() : "";
            // String personName = currentPerson.getDisplayName();
            Person.Name fullName = currentPerson.getName();
            String firstName = fullName.getGivenName();
            String lastName = fullName.getFamilyName();
            int gender = -1;
            if (currentPerson.hasGender()) // it's not guaranteed
                gender = currentPerson.getGender();

            if (gender != -1) {
                if (gender == 0) {
                    GooglePlusGender = "Male";
                } else if (gender == 1) {
                    GooglePlusGender = "Female";
                } else if (gender == 2) {
                    GooglePlusGender = "Others";
                }
            } else {
                GooglePlusGender = "Not Specified";
            }


            Log.i("Google Plus Gender", GooglePlusGender);


            if (currentPerson.hasAgeRange()) {

                int AgeRange = currentPerson.getAgeRange().getMin();
                GoogleAgeRangeString = String.valueOf(AgeRange);

            }

            String lBirthday = "";

            if (currentPerson.hasBirthday()) {
                lBirthday = currentPerson.getBirthday();
//                new CustomLog().info(LOG_TAG, "Birthday::" + lBirthday);
            }

            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
            String personPhoto = currentPerson.getImage().getUrl();
            if (personPhoto.contains("=")) {
                String imageSPlitArray[] = personPhoto.split("=");
                personPhoto = imageSPlitArray[0] + "=200";
                System.out.println(personPhoto);
            }
            String personGooglePlusProfile = currentPerson.getUrl();
//            postGoogleLogin(lSocialId, firstName, lastName, email, GooglePlusGender, GoogleAgeRangeString, personPhoto, lBirthday, lLocation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 0:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setGoogleData();

                } else {
                    showSnackBar("Please grant permission to use this function");
                }
                break;
        }
    }

    private void showSnackBar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();

    }

}
