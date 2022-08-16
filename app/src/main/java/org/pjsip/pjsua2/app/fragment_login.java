package org.pjsip.pjsua2.app;
import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.Fragment;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import org.pjsip.pjsua2.app.R;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_login#newInstance} factory method to
 * create an instance of this fragment.
 */
public class  fragment_login extends Fragment  {


    EditText login,dest,domain;
    EditText password;
    Button send;




    public static String USERNAME =null;  // your account's username
    public static  String DOMAIN =null;   // the domain name of your account
    public static String PASSWORD= null;   // your account's password*/
    public static String DESTINATION=null;
    public SipManager manager = null;
    private SipProfile profile = null;
    public SipAudioCall call = null;
    public SipAudioCall incCall = null;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    public int Register=0;


    public fragment_login() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instanc e of fragment fragment_login.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_login newInstance(String param1, String param2) {
        fragment_login fragment = new fragment_login();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        login = v.findViewById(R.id.et_email);
        //   login.setText("+41445712016");

        USERNAME      = login.getText().toString();
        Log.e("lobe",String.valueOf(USERNAME));

        //USERNAME="+41445712016";
        password = v.findViewById(R.id.et_password);
        //  password.setText("GbAEuJSZP89B");
        //   PASSWORD="GbAEuJSZP89B";


        dest = v.findViewById(R.id.dest);
        //   dest.setText("0041765581035");


        domain=v.findViewById(R.id.domain);






        //  PASSWORD=password.getText().toString();
        //   Log.e("lobe","freetibet");
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.USE_SIP)
                == PackageManager.PERMISSION_GRANTED){
        }else{
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.USE_SIP}, 0);
        }

        send = v.findViewById(R.id.btn_login);
        return v;

    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        // login = (EditText) view.findViewById(R.id.et_email);
        //  USERNAME=login.getText().toString();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.USE_SIP)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "System refuse permission for Sip call ", Toast.LENGTH_SHORT).show();
        }



        // password = (EditText) view.findViewById(R.id.et_password);
        // PASSWORD=password.getText().toString();


        /** initialisation du SIP*/






        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                     USERNAME      = "+41445712016";//login.getText().toString();
                PASSWORD      ="GbAEuJSZP89B";//password.getText().toString();
                DOMAIN ="sip.serv24.com";//domain.getText().toString();
                DESTINATION="0041764193638";//dest.getText().toString();
*/

                USERNAME  = "242424";
                // login.getText().toString();
                PASSWORD = "NpgUqVnaa2fp";     //password.getText().toString();
                DOMAIN = "ucp-fs-dev.serv24.com";   //domain.getText().toString();
                DESTINATION="parrot";

                Log.e("lobe",String.valueOf(USERNAME));


                makeSipManager();
                makeSipProfile();












              /*  scan_Fragment scan= new scan_Fragment();


                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                // fm.beginTransaction().replace(R.id.content01Fragment,scan)
                transaction.replace(R.id.content01Fragment, scan ); // give your fragment container id in first parameter
                transaction.addToBackStack(null);  // if written, this transaction will be added to backstack


                transaction.commit();*/


            }
        });
    }

    private void makeSipManager() {
        // Creates a SipManager to enable calls
        if (manager == null) {
            manager = SipManager.newInstance(getActivity());
            Log.e("$$", "Manager was instantiated");

        }
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
                builder.setProtocol("TCP");
                builder.setPort(5060);
                profile = builder.build();
                Log.e("$$", "SipProfile was built...............");


                // Creates an intent to receive calls
                Intent intent = new Intent();
                intent.setAction("android.VOIPDEMO.INCOMING_CALL");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity() , 0, intent, Intent.FILL_IN_DATA);
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
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(getContext(), "Registration Successful  ", Toast.LENGTH_LONG);
                                        toast.show();
                                    }
                                });
                                Register=Register+1;




                                //  closeAll();

                                final Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {



                                        Intent intent = new Intent(getActivity(), MainActivity2.class);
                                        startActivity(intent);



                                        //Do something after 100ms
                                    }
                                }, 2000);

                                //  Intent intent = new Intent(getActivity(), MainActivity2.class);
                                //      startActivity(intent);

                                closeAll();


                            }

                            /**
                             * Name: onRegistrationFailed
                             * Description: Logs a status message indicating the
                             *              SipProfile failed to register.
                             */
                            public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
                                Log.e("$$", "Sip Profile failed to register <" + localProfileUri + "> " +
                                        " Error message: " + errorMessage);
                                if(Register==0) {

                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast toast = Toast.makeText(getContext(), "Registration failed  ", Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    });
                                }

                            }
                        });
            } catch (ParseException e) {
                Log.e("$$", "SipProfile was not built.");
                e.printStackTrace();
            } catch (SipException e) {
                e.printStackTrace();
            }
        }
    }




}