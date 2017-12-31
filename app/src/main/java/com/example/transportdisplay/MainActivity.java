package com.example.transportdisplay;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.mikepenz.materialdrawer.util.KeyboardUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Button btnDate1, btnDate2, find;
    public static final String ANONYMOUS = "anonymous";
    public static final String ANONYMOUS_PHOTO_URL = "https://firebasestorage.googleapis.com/v0/b/prontonotepad-65983.appspot.com/o/annonymous_user.jpg";
    public static final String ANONYMOUS_EMAIL = "anonymous@noemail.com";

    private TextView txtdate1, txtdate2;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private String username;
    private String photoUrl;
    private String emailAddress;
    private Drawer drawer = null;
    private AccountHeader header = null;
    private Toolbar toolbar;
    private Activity activity;
    private String datetime = "";
    RadioGroup radioGroup;
    Calendar cal1, cal2;
    String uid;
    String deviceName;
    Date fromDate, fromHour;
    Date toDate, toHour;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        cal1 = Calendar.getInstance();
        cal2 = Calendar.getInstance();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        username = user.getDisplayName();
        uid = user.getUid();
        //photoUrl = user.getPhotoUrl().toString();
        emailAddress = user.getEmail();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                } else {
                    username = user.getDisplayName();
                    //photoUrl = user.getPhotoUrl().toString();
                    emailAddress = user.getEmail();

                }
            }
        };
        setupNavigationDrawer(savedInstanceState);

        radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(LinearLayout.HORIZONTAL);

        subscribeToUpdates();
        ((ViewGroup) findViewById(R.id.radiogroup)).addView(radioGroup);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                RadioButton rb = (RadioButton) findViewById(checkedId);
                deviceName = rb.getText().toString();
                Log.d("checked", deviceName);
            }
        });

        btnDate1 = (Button) findViewById(R.id.btndate1);
        btnDate2 = (Button) findViewById(R.id.btndate2);

        find = (Button) findViewById(R.id.find);

        txtdate1 = (TextView) findViewById(R.id.txtdate1);
        txtdate2 = (TextView) findViewById(R.id.txtdate2);


        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        btnDate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TimePickerDialog.OnTimeSetListener callback2 = new TimePickerDialog.OnTimeSetListener() {
                    public void onTimeSet(TimePicker view,
                                          int hourOfDay, int minute) {
                        datetime += ", " + hourOfDay + ":" + minute;
                        txtdate1.setText(datetime);
                        cal1.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        cal1.set(Calendar.MINUTE, minute);
                    }
                };
                final TimePickerDialog time = new TimePickerDialog(
                        MainActivity.this,
                        callback2, 12, 12, true);
                time.setTitle("From hour");

                DatePickerDialog.OnDateSetListener callback = new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear,
                                          int dayOfMonth) {
                        datetime = (dayOfMonth) + "/" + (monthOfYear + 1) + "/" + year;
                        cal1.set(year, monthOfYear, dayOfMonth);
                        time.show();
                    }
                };


                DatePickerDialog pic = new DatePickerDialog(MainActivity.this, callback, 2017, 12, 10);
                pic.setTitle("From date");
                pic.show();


            }
        });

        btnDate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog.OnTimeSetListener callback2 = new TimePickerDialog.OnTimeSetListener() {
                    public void onTimeSet(TimePicker view,
                                          int hourOfDay, int minute) {
                        datetime += ", " + hourOfDay + ":" + minute;
                        txtdate2.setText(datetime);
                        cal2.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        cal2.set(Calendar.MINUTE, minute);

                    }
                };
                final TimePickerDialog time = new TimePickerDialog(
                        MainActivity.this,
                        callback2, 12, 12, true);
                time.setTitle("To hour");
                DatePickerDialog.OnDateSetListener callback = new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear,
                                          int dayOfMonth) {
                        datetime = (dayOfMonth) + "/" + (monthOfYear + 1) + "/" + year;
                        cal2.set(year, monthOfYear, dayOfMonth);
                        time.show();
                    }
                };

                DatePickerDialog pic = new DatePickerDialog(MainActivity.this, callback, 2017, 12, 10);
                pic.setTitle("To date");
                pic.show();

            }
        });

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cal1 == null || cal2 == null || cal2.getTimeInMillis() < cal1.getTimeInMillis()) {
                    Toast.makeText(getApplicationContext(), "The To Date must be greater than The From date!", Toast.LENGTH_SHORT).show();
                } else if (deviceName == null) {
                    Toast.makeText(getApplicationContext(), "You must choose a device", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getBaseContext(), DisplayActivity.class);

                    //Set the Data to pass
                    intent.putExtra("fromDate", cal1.getTimeInMillis());
                    intent.putExtra("toDate", cal2.getTimeInMillis());
                    intent.putExtra("deviceName", deviceName);
                    startActivity(intent);
                }
            }
        });

    }

    int i = 0;
    ArrayMap arrayMap = new ArrayMap();

    private void subscribeToUpdates() {
        // Functionality coming next step

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Devices");

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Devices device = dataSnapshot.getValue(Devices.class);
                //Log.d("aaaaaaaaaaaaaaaaaaa", device.uid);

                if (device.uid.equals(uid)) {
                    RadioButton rdbtn = new RadioButton(activity);
                    rdbtn.setId(i);
                    rdbtn.setText(dataSnapshot.getKey());
                    radioGroup.addView(rdbtn);
                    arrayMap.put(i++, dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Devices device = dataSnapshot.getValue(Devices.class);
                //Log.d("bbbbbbbbbbbbbbbb", device.uid);

                if (device.uid.equals(uid)) {
                    RadioButton rdbtn = new RadioButton(activity);
                    rdbtn.setId(i);
                    rdbtn.setText(dataSnapshot.getKey());
                    radioGroup.addView(rdbtn);
                    arrayMap.put(i++, dataSnapshot.getKey());
                } else {
                    for (int j = 0; j < arrayMap.size(); j++) {
                        Log.d("rrrrrrr " + j, "" + arrayMap.get(j));
                        if (arrayMap.get(j) != null) {
                            if (arrayMap.get(j).equals(dataSnapshot.getKey())) {
                                radioGroup.removeViewAt(j);
                            }
                        }
                    }

                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Devices device = dataSnapshot.getValue(Devices.class);
                //Log.d("bbbbbbbbbbbbbbbb", device.uid);

                if (device.uid.equals(uid)) {
                    RadioButton rdbtn = new RadioButton(activity);
                    rdbtn.setId(i);
                    rdbtn.setText(dataSnapshot.getKey());
                    radioGroup.addView(rdbtn);
                    arrayMap.put(i++, dataSnapshot.getKey());
                } else {
                    for (int j = 0; j < arrayMap.size(); j++) {
                        Log.d("rrrrrrr " + j, "" + arrayMap.get(j));
                        if (arrayMap.get(j) != null) {
                            if (arrayMap.get(j).equals(dataSnapshot.getKey())) {
                                radioGroup.removeViewAt(j);
                            }
                        }
                    }

                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Devices device = dataSnapshot.getValue(Devices.class);
                //Log.d("bbbbbbbbbbbbbbbb", device.uid);

                if (device.uid.equals(uid)) {
                    RadioButton rdbtn = new RadioButton(activity);
                    rdbtn.setId(i);
                    rdbtn.setText(dataSnapshot.getKey());
                    radioGroup.addView(rdbtn);
                    arrayMap.put(i++, dataSnapshot.getKey());
                } else {
                    for (int j = 0; j < arrayMap.size(); j++) {
                        Log.d("rrrrrrr " + j, "" + arrayMap.get(j));
                        if (arrayMap.get(j) != null) {
                            if (arrayMap.get(j).equals(dataSnapshot.getKey())) {
                                radioGroup.removeViewAt(j);
                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.d(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    private void setupNavigationDrawer(Bundle savedInstanceState) {
        username = TextUtils.isEmpty(username) ? ANONYMOUS : username;
        emailAddress = TextUtils.isEmpty(emailAddress) ? ANONYMOUS_EMAIL : emailAddress;
        photoUrl = TextUtils.isEmpty(photoUrl) ? ANONYMOUS_PHOTO_URL : photoUrl;

        IProfile profile = new ProfileDrawerItem()
                .withName(username)
                .withEmail(emailAddress)
                .withIcon(photoUrl)
                .withIdentifier(102);

        header = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(profile)
                .build();
        drawer = new DrawerBuilder()
                .withAccountHeader(header)
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Realtime").withIcon
                                (GoogleMaterial.Icon.gmd_view_list).withIdentifier(Constants.Realtime),
                        new PrimaryDrawerItem().withName("History").withIcon
                                (GoogleMaterial.Icon.gmd_history).withIdentifier(Constants.History),
                        new PrimaryDrawerItem().withName("Manage Account").withIcon
                                (GoogleMaterial.Icon.gmd_supervisor_account).withIdentifier(Constants.Setting),
                        new PrimaryDrawerItem().withName("Logout").withIcon
                                (GoogleMaterial.Icon.gmd_all_out).withIdentifier(Constants.Logout)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null && drawerItem instanceof Nameable) {
                            String name = ((Nameable) drawerItem).getName().getText(activity);
                            toolbar.setTitle(name);
                        }

                        if (drawerItem != null) {
                            //handle on navigation drawer item
                            onTouchDrawer((int) drawerItem.getIdentifier());
                        }
                        return false;
                    }
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        KeyboardUtil.hideKeyboard(activity);
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {

                    }
                })
                .withFireOnInitialOnClick(true)
                .withSavedInstance(savedInstanceState)
                .build();

    }

    private void onTouchDrawer(int position) {
        switch (position) {
            case Constants.Realtime:
                //Do Nothing, we are already on Notes
                if (deviceName == null) {
                    Toast.makeText(getApplicationContext(), "You must choose a device", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getBaseContext(), DisplayActivity.class);

                    intent.putExtra("deviceName", deviceName);
                    startActivity(intent);
                }
                break;
            case Constants.History:
                //Todo - Implement Firebase Auth Logout
                break;
            case Constants.Setting:
                //Todo - Implement Firebase Auth Login

                break;
            case Constants.Logout:
                //Delete Account
                //Todo - Implement Firebase Auth Delete Account
                signOut();
                break;
        }
    }

    //sign out method
    public void signOut() {
        auth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

}
