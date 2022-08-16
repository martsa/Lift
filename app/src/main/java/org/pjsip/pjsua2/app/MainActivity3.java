package org.pjsip.pjsua2.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ParseException;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity3 extends AppCompatActivity implements View.OnClickListener {




    EditText login;
    EditText password;
    Button send;


    private final String USERNAME ="90701871890";  // your account's username
    private final String DOMAIN = "sips.peoplefone.ch";     // the domain name of your account
    private final String PASSWORD= "p3KB7sgh9c8c";   // your account's password
    public SipManager manager = null;
    private SipProfile profile = null;
    public SipAudioCall call = null;
    public SipAudioCall incCall = null;
    public static int Reg=0;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public int Register=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        send = (Button)findViewById(R.id.btn_login);
        login = (EditText) findViewById(R.id.et_email);
        login.setText("lobsangmartsa");
        password = (EditText) findViewById(R.id.et_password);
        password.setText("Freetibet123");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_SIP)
                == PackageManager.PERMISSION_GRANTED){
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.USE_SIP}, 0);
        }
        send.setOnClickListener((this));
/*

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //  account.setRegistration(false);

                makeSipManager();
                makeSipProfile();


                Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                startActivity(intent);



              /*
                              fragment_login fragment = new fragment_login();


                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                // fm.beginTransaction().replace(R.id.content01,Fragment,scan)
                transaction.replace(R.id.content01Fragment, scan ); // give your fragment container id in first parameter
                transaction.addToBackStack(null);  // if written, this transaction will be added to backstack


                transaction.commit();*/
/*



            }
        });*/

    }


    private void makeSipManager() {
        // Creates a SipManager to enable calls
        if (manager == null) {
            manager = SipManager.newInstance(this);
            Log.e("$$", "Manager was instantiated");

        }
    }



    @Override
    public void onClick(View v)
    {
        makeSipManager();
        makeSipProfile();


        Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
        startActivity(intent);



    }


    private void closeAll()
    {
        try { // try to close everything
            manager.close(profile.getUriString());


        } catch (SipException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }



    private void makeSipProfile() {

        if (manager != null) {
            // Creates a SipProfile for the User
            try {
                SipProfile.Builder builder = new SipProfile.Builder(USERNAME, DOMAIN);
                builder.setPassword(PASSWORD);
                profile = builder.build();
                Log.e("$$", "SipProfile was built...............");


                // Creates an intent to receive calls
                Intent intent = new Intent();
                intent.setAction("android.VOIPDEMO.INCOMING_CALL");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this , 0, intent, Intent.FILL_IN_DATA);
                manager.open(profile, pendingIntent, null);


                // Determines if the SipProfile successfully registered
                manager.setRegistrationListener(profile.getUriString(),
                        new SipRegistrationListener() {

                            /**
                             * Name: onRegistering
                             * Description: Logs a status message indicating the
                             *              SipProfile is registering.
                             */
                            public void onRegistering(String localProfileUri) {
                                Log.e("$$", "Sip Profile <" + localProfileUri + " is registering");
                            }

                            /**
                             * Name: onRegistrationDone
                             * Description: Logs a status message indicating the
                             *              SipProfile successfully registered.
                             */
                            public void onRegistrationDone(String localProfileUri, long expiryTime) {
                                Log.e("$$", "Sip Profile <" + localProfileUri + "> successfully registered");
                                Register=007;




                                closeAll();

                                final Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {



                                        Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                                        startActivity(intent);



                                        //Do something after 100ms
                                    }
                                }, 2000);

                                //  Intent intent = new Intent(getActivity(), MainActivity2.class);
                                //      startActivity(intent);



                            }

                            /**
                             * Name: onRegistrationFailed
                             * Description: Logs a status message indicating the
                             *              SipProfile failed to register.
                             */
                            public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
                                Log.e("$$", "Sip Profile failed to register <" + localProfileUri + "> " +
                                        " Error message: " + errorMessage);
                                Register=006;

                            }
                        });
            } catch (ParseException e) {
                Log.e("$$", "SipProfile was not built.");
                e.printStackTrace();
            } catch (SipException e) {
                e.printStackTrace();
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }


}}