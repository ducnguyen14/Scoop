package com.example.instaclone.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.instaclone.Home.HomeActivity;
import com.example.instaclone.Profile.AccountSettingsActivity;
import com.example.instaclone.R;
import com.example.instaclone.models.Photo;
import com.example.instaclone.models.User;
import com.example.instaclone.models.UserAccountSettings;
import com.example.instaclone.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods/DEBUG";

    private boolean debug = false;


    // Notes: Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;
    private String userID;

    // Notes: Variables
    private Context mContext;
    private double mPhotoUploadProgress = 0;




    public FirebaseMethods(Context context)
    {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();
        mContext = context;


        if(mAuth.getCurrentUser() != null)
        {
            userID = mAuth.getCurrentUser().getUid();
        }

    }



    public void uploadNewPhoto(String photoType, final String caption, int count, String imgUrl, Bitmap bm)
    {
        Log.d(TAG, "\tuploadNewPhoto: attempting to upload new photo");

        FilePaths filePaths = new FilePaths();

        // Notes: Case 1 - New Photo
        if(photoType.equals(mContext.getString(R.string.new_photo)))
        {
            Log.d(TAG, "\tuploadNewPhoto: uploading New Photo");

            // Notes: TODO - Double check if we can just use userID or if there's a problem with the global var
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Notes: Need to pass the paths to the location
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + (count + 1));


            // Notes: Convert image URL to bitmap
            if(bm == null)
            {
                bm = ImageManager.getBitmap(imgUrl);
            }


            // Notes: Convert bitmap to byte[] --> TODO: Play with the quality to find the sweet spot of a decent photo quality
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);



            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {

                    // Notes: TODO - Code below does not work (Part 54)
                    // Uri firebaseUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().getResult();
                    // Notes: Solution to above:

                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                    {
                        @Override
                        public void onSuccess(Uri uri)
                        {
                            // Notes: Storing the pointer into Firebase Database

                            // Notes: Step 1 - Add the new photo to 'photos' node and 'user_photos' node

                            Uri firebaseUrl = uri;

                            addPhotoToDatabase(caption, firebaseUrl.toString());

                            Toast.makeText(mContext, "Photo upload success", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "\tonSuccess: Uri ---> " + firebaseUrl.toString());



                            // Notes: Navigate to the main feed so the user can see their photo
                            Intent intent = new Intent(mContext, HomeActivity.class);
                            mContext.startActivity(intent);

                        }
                    });







                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Log.d(TAG, "\tonFailure: Photo Upload Failed." + e.getMessage());
                    Toast.makeText(mContext, "Photo Upload Failed", Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot)
                {
                    double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());

                    // Notes: Toast will not show unless the new progress is at least 15 higher than the previous progress
                    if(progress - 15 > mPhotoUploadProgress)
                    {
                        Toast.makeText(mContext, "Photo Upload Progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "\tonProgress: upload progress: " + progress + "% done");

                }
            });




        }

        // Notes: Case 2 - New profile photo
        else if(photoType.equals(mContext.getString(R.string.profile_photo)))
        {
            Log.d(TAG, "\tuploadNewPhoto: uploading new Profile Photo");


            // Notes: TODO - Double check if we can just use userID or if there's a problem with the global var
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Notes: Need to pass the paths to the location
            StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");



            // Notes: Convert image URL to bitmap
            if(bm == null)
            {
                bm = ImageManager.getBitmap(imgUrl);
            }

            // Notes: Convert bitmap to byte[] --> TODO: Play with the quality to find the sweet spot of a decent photo quality
            byte[] bytes = ImageManager.getBytesFromBitmap(bm, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);



            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    // Notes: TODO - Code below does not work (Part 54)
                    // Uri firebaseUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().getResult();
                    // Notes: Solution to above:

                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                    {
                        @Override
                        public void onSuccess(Uri uri)
                        {
                            // Notes: Storing the pointer into Firebase Database

                            // Notes: Step 1 - Add the new photo to 'photos' node and 'user_photos' node

                            Uri firebaseUrl = uri;


                            // Notes: Insert into 'user_account_settings' node
                            setProfilePhoto(firebaseUrl.toString());

                            // Notes: Set the viewpager on AccountSettingsActivity to the EditProfileFragment
                            ((AccountSettingsActivity)mContext).setViewPager(
                                    ((AccountSettingsActivity)mContext).pagerAdapter
                                            .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment)));


                            Toast.makeText(mContext, "Photo upload success", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "\tonSuccess: Uri ---> " + firebaseUrl.toString());

                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Log.d(TAG, "\tonFailure: Photo Upload Failed." + e.getMessage());
                    Toast.makeText(mContext, "Photo Upload Failed", Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot)
                {
                    double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());

                    // Notes: Toast will not show unless the new progress is at least 15 higher than the previous progress
                    if(progress - 15 > mPhotoUploadProgress)
                    {
                        Toast.makeText(mContext, "Photo Upload Progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "\tonProgress: upload progress: " + progress + "% done");

                }
            });


        }


    }


    private void setProfilePhoto(String url)
    {
        Log.d(TAG, "\tsetProfilePhoto: setting new profile photo: " + url);


        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);


    }



    private String getTimestamp()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        return sdf.format(new Date());

    }




    private void addPhotoToDatabase(String caption, String url)
    {
        Log.d(TAG, "\taddPhotoToDatabase: Adding photo to database");

        // Notes: Each photo has its own separate ID
        String newPhotoKey = myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();

        String tags = StringManipulation.getTags(caption);

        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimestamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());
        photo.setPhoto_id(newPhotoKey);


        // Notes: Insert into database
        myRef.child(mContext.getString(R.string.dbname_user_photos))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(newPhotoKey)
                .setValue(photo);

        myRef.child(mContext.getString(R.string.dbname_photos))
                .child(newPhotoKey)
                .setValue(photo);


    }



    public int getImageCount(DataSnapshot dataSnapshot)
    {
        int count = 0;
        for(DataSnapshot ds: dataSnapshot
                .child(mContext.getString(R.string.dbname_user_photos))
                .child(userID)
                .getChildren())
        {
            // Notes: For every child found/iterated, we just want to increase count
            count++;
        }

        return count;

    }




    /**
     * Notes: Updates users and user_account_settings nodes for the current user
     * @param displayName
     * @param website
     * @param description
     * @param phoneNumber
     */
    public void updateUserAccountSettings(String displayName, String website, String description, long phoneNumber)
    {
        Log.d(TAG, "\tupdateUserAccountSettings: updating user account settings.");

        if(displayName != null)
        {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_display_name))
                    .setValue(displayName);
        }

        if(website != null)
        {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_website))
                    .setValue(website);
        }

        if(description != null)
        {
            myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                    .child(userID)
                    .child(mContext.getString(R.string.field_description))
                    .setValue(description);
        }

        if(phoneNumber != 0)
        {
            myRef.child(mContext.getString(R.string.dbname_users))
                    .child(userID)
                    .child(mContext.getString(R.string.field_phone_number))
                    .setValue(phoneNumber);
        }






    }




    /**
     *  Notes: Update the email in the users node and user_account_settings node
     */
    public void updateUsername(String username)
    {
        Log.d(TAG, "\tupdateUsername: updating username to: " + username);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);

        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    /**
     *  Notes: Update the email in the users node
     */
    public void updateEmail(String email)
    {
        Log.d(TAG, "\tupdateEmail: updating email to: " + email);

        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.field_email))
                .setValue(email);

    }



//    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot)
//    {
//        Log.d(TAG, "\tcheckIfUsernameExists: checking if " + username + " already exists.");
//
//        User user = new User();
//
//        // Notes - TODO - How to loop inside a datasnapshot
//        if(debug == true)
//        {
//            Log.d(TAG, "\tcheckIfUsernameExists: datasnapshot: " + dataSnapshot);
//            Log.d(TAG, "\tcheckIfUsernameExists: datasnapshot.getChildren(): " + dataSnapshot.getChildren());
//            Log.d(TAG, "\tcheckIfUsernameExists: NODE = " + dataSnapshot.child(userID).toString());
//            Log.d(TAG, "\tcheckIfUsernameExists: NODE = " + dataSnapshot.child(userID).getChildren().toString());
//
//            for (DataSnapshot ds: dataSnapshot.getChildren())
//            {
//                Log.d(TAG, "\tcheckIfUsernameExists: ds: " + ds);
//                User user2 = new User();
//                Log.d(TAG, "\tcheckIfUsernameExists: getValue(User.class)" + ds.getValue(User.class).toString());
//                Log.d(TAG, "\tcheckIfUsernameExists: getValue(User.class)" + ds.getValue(User.class).getUsername());
//                user2 = ds.getValue(User.class);
////            user2.setUsername(ds.getValue(User.class).getUsername());
//                Log.d(TAG, "\tcheckIfUsernameExists: username: " + user2.getUsername());
//            }
//        }
//
//
//        // Notes: TODO - The loop below doesn't even go check the nodes
//        /*
//            Notes: Loop through DataSnapshot to check for same username.
//                DataSnapshot allows us to see what's inside the database
//                because it contains every node inside the database.
//         */
//        for (DataSnapshot ds: dataSnapshot.child(userID).getChildren())
//        {
//            Log.d(TAG, "\tcheckIfUsernameExists: datasnapshot: " + ds);
//
//            user.setUsername(ds.getValue(User.class).getUsername());
//            Log.d(TAG, "\tcheckIfUsernameExists: username: " + user.getUsername());
//
//            // Notes: TODO - Rewrite this line to make it easier to read
//            if(StringManipulation.expandUsername(user.getUsername()).equals(username))
//            {
//                Log.d(TAG, "\tcheckIfUsernameExists: Found a match: " + user.getUsername());
//                return true;
//            }
//        }
//
//        return false;
//
//
//
//    }


    /**
     * Notes: This method is for registering a new email
     *      and password to Firebase Authentication
     * @param email
     * @param password
     * @param username
     */
    public void registerNewEmail(final String email, String password, final String username)
    {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (!task.isSuccessful())
                        {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "\tcreateUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, R.string.auth_failed, Toast.LENGTH_SHORT).show();

                        }
                        else
                        {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "\tcreateUserWithEmail:success");

                            // Notes: Sending Verification email
                            sendVerificationEmail();

                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "\tonComplete: Authstate changed " + userID);

                        }

                        // ...
                    }
                });
    }


    public void sendVerificationEmail()
    {
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null)
        {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            // Notes: Not Successful
                            if(!task.isSuccessful())
                            {
                                Toast.makeText(mContext, "Couldn't send verification email.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }




    /**
     * Notes: Add information to the users nodes
     *      Add information to the user_account_settings node.
     */
    public void addNewUser(String email, String username, String description, String website, String profile_photo)
    {
        User user = new User(userID, 1, email, StringManipulation.condenseUsername(username));

        // Notes: DEBUG tools
        if(debug == true)
        {
            Log.d(TAG, "\taddNewUser: myRef = " + myRef.child(mContext.getString(R.string.dbname_users)).toString());
            Log.d(TAG, "\taddNewUser: myRef = " + myRef.child(mContext.getString(R.string.dbname_users)).child(userID).toString());
        }


        // Notes: Inserting user into Firebase Database.
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);

        // Notes: Universal Image Loader can handle an empty string, it will load the default image
        UserAccountSettings settings = new UserAccountSettings(
                description,
                username,
                0,
                0,
                0,
                profile_photo,
                StringManipulation.condenseUsername(username),
                website,
                userID);

        // Notes: Inserting user_account_settings into Firebase Database.
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);



    }

    /**
     * Notes: This method retrieves the account settings for the user currently logged in
     *      from the user_account_settings node
     * @param dataSnapshot
     * @return
     */

    public UserSettings getUserSettings(DataSnapshot dataSnapshot)
    {
        Log.d(TAG, "\tgetUserAccountSettings: retrieviing user account settings from firebase");


        UserAccountSettings settings = new UserAccountSettings();
        User user = new User();

        // Notes: Looping through the major nodes
        for(DataSnapshot ds: dataSnapshot.getChildren())
        {
            // Notes: if ds.getKey() == user_account_settings node
            if(ds.getKey().equals(mContext.getString(R.string.dbname_user_account_settings)))
            {
                Log.d(TAG, "\tgetUserAccountSettings: datasnapshot: " + ds);

                // Notes: Try-Catch just in case of null fields
                try
                {
                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDisplay_name()
                    );
                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getUsername()
                    );
                    settings.setWebsite(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getWebsite()
                    );
                    settings.setDescription(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getDescription()
                    );
                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getProfile_photo()
                    );
                    settings.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getPosts()
                    );
                    settings.setFollowing(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowing()
                    );
                    settings.setFollowers(
                            ds.child(userID)
                                    .getValue(UserAccountSettings.class)
                                    .getFollowers()
                    );

                    Log.d(TAG, "\tgetUserAccountSettings: retrieved user_account_settings information" + settings.toString());
                }
                catch (NullPointerException e)
                {
                    Log.e(TAG, "\tgetUserAccountSettings: " + e.getMessage());
                }
            }

            // Notes: if ds.getKey() == users node
            if(ds.getKey().equals(mContext.getString(R.string.dbname_users)))
            {
                Log.d(TAG, "\tgetUserAccountSettings: datasnapshot: " + ds);

                try {
                    user.setUsername(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getUsername()
                    );
                    user.setEmail(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getEmail()
                    );
                    user.setPhone_number(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getPhone_number()
                    );
                    user.setUser_id(
                            ds.child(userID)
                                    .getValue(User.class)
                                    .getUser_id()
                    );

                    Log.d(TAG, "\tgetUserAccountSettings: retrieved users information" + user.toString());


                }
                catch (NullPointerException e)
                {
                    Log.e(TAG, "\tgetUserAccountSettings: " + e.getMessage());

                }



            }



        }


        return new UserSettings(user, settings);


    }



}
