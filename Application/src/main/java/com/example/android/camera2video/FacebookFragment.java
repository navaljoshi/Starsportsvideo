package com.example.android.camera2video;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class FacebookFragment extends Fragment {

    private LoginButton loginButton;
    private Button getUserInterests;
    private boolean postingEnabled = false;

    private static final String PERMISSION = "publish_actions";
    private final String PENDING_ACTION_BUNDLE_KEY =
            "com.example.hellofacebook:PendingAction";

    private Button postStatusUpdateButton;
    private Button postPhotoButton;
    private ImageView profilePicImageView;
    private TextView greeting;
    private PendingAction pendingAction = PendingAction.NONE;
    private boolean canPresentShareDialog;
    private boolean canPresentShareDialogWithPhotos;
    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;
    private ShareDialog shareDialog;
    ProgressDialog progressdialog;
    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onCancel() {
            getActivity().finish();//chance vaibhav

            Log.d("FacebookFragment", "Canceled");
        }

        @Override
        public void onError(FacebookException error) {
            Log.d("FacebookFragment", String.format("Error: %s", error.toString()));
            String title = getString(R.string.error);
            String alertMessage = error.getMessage();
            showResult(title, alertMessage);
        }

        @Override
        public void onSuccess(Sharer.Result result) {
            Log.d("FacebookFragment", "Success!");

            if (result.getPostId() != null) {
                String title = getString(R.string.success);
                progressdialog.dismiss();
                Toast.makeText(getActivity(), "You are Signed Out from FB !!.", Toast.LENGTH_LONG).show();
                // getView().getParent()
                String id = result.getPostId();
                String alertMessage = getString(R.string.successfully_posted_post, id);
                // showResult(title, alertMessage);
                Log.d("naval", " onSuccess");
                getActivity().finish();//chance vaibhav


            }
        }

        private void showResult(String title, String alertMessage) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(alertMessage)
                    .setPositiveButton(R.string.ok, null)
                    .show();
            Log.d("naval", " showResult");
        }
    };

    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity());
        Log.d("naval", " onCreate");
        // Other app specific specialization

        // Other app specific specialization
        final PendingIntent intent = PendingIntent.getActivity(getActivity().getApplication(), 0,
                new Intent(getActivity().getIntent()), PendingIntent.FLAG_CANCEL_CURRENT);
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable ex) {
                AlarmManager mgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, intent);
                System.exit(2);
                defaultHandler.uncaughtException(thread, ex);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_facebook, parent, false);
        loginButton = (LoginButton) v.findViewById(R.id.loginButton);
        // loginButton.performClick(); // moving stright to FORM login page
        Log.d("navall", " Indide View ");
        // If using in a fragment
        loginButton.setFragment(this);
        callbackManager = CallbackManager.Factory.create();
        progressdialog = new ProgressDialog(getActivity());
        progressdialog.setMessage("Please Wait....");
        // Callback registration

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast toast = Toast.makeText(getActivity(), "Logged In", Toast.LENGTH_SHORT);
                postingEnabled = true;
                postPhotoButton.setVisibility(View.VISIBLE);
                postStatusUpdateButton.setVisibility(View.INVISIBLE);
                getUserInterests.setVisibility(View.INVISIBLE);

                toast.show();
                handlePendingAction();
                updateUI();

                Log.d("naval", " onSuccess INSIDE onCreateView");
                //postPhotoButton.performClick();

                //  if(MainActivity.videoflag)
                // {
                Log.d("lvmh", "inside video post case");
                postVideo();

                //  }
                //   else
                //   {
                Log.d("lvmh", "inside photo post case");
                //   postPhoto();


                //   }


            }

            @Override
            public void onCancel() {
                // App code
                if (pendingAction != PendingAction.NONE) {
                    showAlert();
                    pendingAction = PendingAction.NONE;
                    getActivity().finish();//chance vaibhav

                }
                getActivity().finish();//chance vaibhav

                // MainActivity.fbFlagCancel = true;


                updateUI();
            }

            @Override
            public void onError(FacebookException exception) {
                if (pendingAction != PendingAction.NONE
                        && exception instanceof FacebookAuthorizationException) {
                    showAlert();
                    pendingAction = PendingAction.NONE;
                }
                updateUI();

            }

            private void showAlert() {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.cancelled)
                        .setMessage(R.string.permission_not_granted)
                        .setPositiveButton(R.string.ok, null)
                        .show();
                Log.d("naval", " showAlert");
            }

        });
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList(PERMISSION));
        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(
                callbackManager,
                shareCallback);

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }


        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                updateUI();
                handlePendingAction();
            }
        };


        profilePicImageView = (ImageView) v.findViewById(R.id.profilePicture);
        greeting = (TextView) v.findViewById(R.id.greeting);


        Log.d("naval", " Adding button listners ");
        postStatusUpdateButton = (Button) v.findViewById(R.id.postStatusUpdateButton);
        postStatusUpdateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d("naval", " postStatusUpdateButton ");
                onClickPostStatusUpdate();
            }
        });

        postPhotoButton = (Button) v.findViewById(R.id.postPhotoButton);
        postPhotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.d("naval", " postPhotoButton ");
                onClickPostPhoto();
            }
        });

        getUserInterests = (Button) v.findViewById(R.id.getInterestsButton);

        getUserInterests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                if (object != null) {
                                    Log.d("Flogin", object.toString());
                                    String name = JSONParser.getName(object);
                                    String id = JSONParser.getId(object);
                                    ArrayList<String> favAthletes = JSONParser.getFavAthletes(object);
                                    ArrayList<String> favTeams = JSONParser.getFavTeams(object);
                                    String s = "Name : " + name + "\n";
                                    s += "Id : " + id + "\n";
                                    s += "Favourite Athletes : " + "\n";
                                    for (int i = 0; i < favAthletes.size(); i++) {
                                        s += ((i + 1) + ". " + favAthletes.get(i)).toString() + "\n";
                                    }

                                    s += "Favourite Teams : " + "\n";
                                    for (int i = 0; i < favTeams.size(); i++) {
                                        s += ((i + 1) + ". " + favTeams.get(i)).toString() + "\n";
                                    }

                                    Toast t = Toast.makeText(getActivity(), s, Toast.LENGTH_LONG);
                                    t.show();
                                }

                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,link,favorite_athletes,favorite_teams");
                request.setParameters(parameters);
                request.executeAsync();

            }
        });


        // Can we present the share dialog for regular links?
        canPresentShareDialog = ShareDialog.canShow(
                ShareLinkContent.class);

        // Can we present the share dialog for photos?
        canPresentShareDialogWithPhotos = ShareDialog.canShow(
                SharePhotoContent.class);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("naval", " On view click  ");

                // LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile"));
                if (!postingEnabled) {

                    postingEnabled = true;
                    postPhotoButton.setVisibility(View.VISIBLE);
                    postStatusUpdateButton.setVisibility(View.INVISIBLE);
                    getUserInterests.setVisibility(View.INVISIBLE);


                } else {

                    postingEnabled = false;
                    postPhotoButton.setVisibility(View.GONE);
                    postStatusUpdateButton.setVisibility(View.GONE);
                    getUserInterests.setVisibility(View.GONE);

                }


            }
        });


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onResume methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.activateApp(getActivity());

        updateUI();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onPause methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.deactivateApp(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
    }


    private void updateUI() {
        //Log.d("naval"," updateUI ");
        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;

        postStatusUpdateButton.setEnabled(enableButtons || canPresentShareDialog);
        postPhotoButton.setEnabled(enableButtons || canPresentShareDialogWithPhotos);

        Profile profile = Profile.getCurrentProfile();
        if (enableButtons && profile != null) {
            new LoadProfileImage(profilePicImageView).execute(profile.getProfilePictureUri(200, 200).toString());
            greeting.setText(getString(R.string.hello_user, profile.getFirstName()));
            postingEnabled = true;
            postPhotoButton.setVisibility(View.VISIBLE);
            postStatusUpdateButton.setVisibility(View.VISIBLE);
            getUserInterests.setVisibility(View.VISIBLE);

        } else {
            Bitmap icon = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.user_default);
            profilePicImageView.setImageBitmap(ImageHelper.getRoundedCornerBitmap(getContext(), icon, 200, 200, 200, false, false, false, false));
            greeting.setText(null);
            postingEnabled = false;
            postPhotoButton.setVisibility(View.GONE);
            postStatusUpdateButton.setVisibility(View.GONE);
            getUserInterests.setVisibility(View.GONE);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
    }

    private void handlePendingAction() {
        Log.d("naval", " handlePendingAction ");
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {
            case NONE:
                break;
            case POST_PHOTO:

                Log.d("lvmh", "inside photo post case");
                postPhoto();

            case POST_STATUS_UPDATE:
                postStatusUpdate();
                break;
        }
    }


    public byte[] readBytes(String dataPath) throws IOException {

        InputStream inputStream = new FileInputStream(dataPath);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }

        return byteBuffer.toByteArray();
    }


    private void onClickPostStatusUpdate() {
        performPublish(PendingAction.POST_STATUS_UPDATE, canPresentShareDialog);
    }

    private void postStatusUpdate() {
        Log.d("naval", " postStatusUpdate ");
        Profile profile = Profile.getCurrentProfile();
        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                .setContentTitle("Integrate Facebook Login to your Android App")
                .setContentDescription(
                        "This app shows how to integrate Facebook Login to your Android App")
                .setContentUrl(Uri.parse("http://www.androidtutorialpoint.com/material-design/adding-facebook-login-to-android-app/"))
                .build();
        if (canPresentShareDialog) {
            shareDialog.show(linkContent);
        } else if (profile != null && hasPublishPermission()) {
            ShareApi.share(linkContent, shareCallback);
        } else {
            pendingAction = PendingAction.POST_STATUS_UPDATE;
        }
    }


    private void onClickPostPhoto() {
        performPublish(PendingAction.POST_PHOTO, canPresentShareDialogWithPhotos);
    }


    private void postVideo() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        GraphRequest request = GraphRequest.newPostRequest(accessToken, "me/videos", null, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                if (response.getError() == null) {
                    //failed to load video
                }
                LoginManager.getInstance().logOut();
            }
        });
        Bundle params = request.getParameters();
        try {
            byte[] data = readBytes(Camera2VideoFragment.imagePath);
            params.putByteArray("video.mp4", data);
            //params.putString("title", albumName);
            params.putString("description", " #Toyota-nj");
            request.setParameters(params);
            request.executeAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void postPhoto() {
        Log.d("naval", " postPhoto ");
        String fbImagePath = Camera2VideoFragment.imagePath;

        // Drawable img = Drawable.createFromPath(MainActivity.imagePath);
        // Bitmap image = BitmapFactory.decodeResource(this.getResources(), img);


        Bitmap image = BitmapFactory.decodeFile(Camera2VideoFragment.imagePath.toString());


        Log.d("naval", " here is the final FB photo path :" + fbImagePath);
        progressdialog.show();

        SharePhoto sharePhoto = new SharePhoto.Builder().setBitmap(image).build();
        ArrayList<SharePhoto> photos = new ArrayList<>();
        photos.add(sharePhoto);

        SharePhotoContent sharePhotoContent =
                new SharePhotoContent.Builder().setPhotos(photos).build();
        if (canPresentShareDialogWithPhotos) {
            shareDialog.show(sharePhotoContent);
        } else if (hasPublishPermission()) {
            ShareApi.share(sharePhotoContent, shareCallback);
        } else {
            pendingAction = PendingAction.POST_PHOTO;
            // We need to get new permissions, then complete the action when we get called back.
            // LoginManager.getInstance().logInWithPublishPermissions(
            //       this,
            //     Arrays.asList(PERMISSION));
        }
        LoginManager.getInstance().logOut(); // naval logout
        Log.d("naval", "Logout of FB and posted image ");
    }

    private boolean hasPublishPermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
    }

    private void performPublish(PendingAction action, boolean allowNoToken) {
        Log.d("naval", " performPublish ");
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null || allowNoToken) {
            pendingAction = action;
            handlePendingAction();
        }
    }

    /**
     * Background Async task to load user profile picture from url
     */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }


        protected Bitmap doInBackground(String... uri) {
            String url = uri[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {

            if (result != null) {


                Bitmap resized = Bitmap.createScaledBitmap(result, 200, 200, true);
                bmImage.setImageBitmap(ImageHelper.getRoundedCornerBitmap(getContext(), resized, 250, 200, 200, false, false, false, false));
            }
        }
    }
}





