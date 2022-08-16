package org.pjsip.pjsua2.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import android.media.AudioManager;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageButton;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.widget.Toast;
import org.pjsip.pjsua2.*;


public class MainActivity2 extends AppCompatActivity implements Handler.Callback, MyAppObserver,View.OnClickListener, AdapterView.OnItemSelectedListener{

    ImageView imageView;
    Button button;
    Button btnScan;
    Thread thread ;
    public final static int QRcodeWidth = 350 ;
    Bitmap bitmap ;
    TextView tv_qr_readTxt;
    CheckBox checkBox1,checkBox2,checkBox3,checkBox4,checkBoxer;
    StringBuilder result;
    String liftError;
    EditText liftname;
    String barCode =null;
    public static MyAppObserver observer;

    private static final int REQUEST_DANGEROUS_PERMISSIONS = 108;

    /***************************1*************************/
    public static MyApp app = null;
    public static MyCall currentCall = null;
    public static MyAccount account = null;
    public static AccountConfig accCfg = null;
    public static MyBroadcastReceiver receiver = null;
    public static IntentFilter intentFilter = null;

    private ListView buddyListView;
    private SimpleAdapter buddyListAdapter;
    private int buddyListSelectedIdx = -1;
    ArrayList<Map<String, String>> buddyList;
    private String lastRegStatus = "";
    private String subdomain;
    public String msg_str = "";

    public static String USERNAME =null;  // your account's username
    public static  String DOMAIN =null;   // the domain name of your account
    public static String PASSWORD= null;   // your account's password*/
    public static String PORTS=null;
    public static String DESTINATION=null;
    public static int Reg=0;


    EditText login,port,domain;
    EditText password;
    String[] users = { "TCP", "UDP", "TLS" };






    private final Handler handler = new Handler(this);
    public class MSG_TYPE
    {
        public final static int INCOMING_CALL = 1;
        public final static int CALL_STATE = 2;
        public final static int REG_STATE = 3;
        public final static int BUDDY_STATE = 4;
        public final static int CALL_MEDIA_STATE = 5;
        public final static int CHANGE_NETWORK = 6;
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        private String conn_name = "";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isNetworkChange(context))
                notifyChangeNetwork();
        }

        private boolean isNetworkChange(Context context) {
            boolean network_changed = false;
            ConnectivityManager connectivity_mgr =
                    ((ConnectivityManager)context.getSystemService(
                            Context.CONNECTIVITY_SERVICE));

            NetworkInfo net_info = connectivity_mgr.getActiveNetworkInfo();
            if(net_info != null && net_info.isConnectedOrConnecting() &&
                    !conn_name.equalsIgnoreCase(""))
            {
                String new_con = net_info.getExtraInfo();
                if (new_con != null && !new_con.equalsIgnoreCase(conn_name))
                    network_changed = true;

                conn_name = (new_con == null)?"":new_con;
            } else {
                if (conn_name.equalsIgnoreCase(""))
                    conn_name = net_info.getExtraInfo();
            }
            return network_changed;
        }
    }

    private HashMap<String, String> putData(String uri, String status)
    {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("uri", uri);
        item.put("status", status);
        return item;
    }

    private void showCallActivity()
    {
        Intent intent = new Intent(this, CallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
    }


    /***************************1*************************/







    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            checkFieldsForEmptyValues();
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);


        login =(EditText)findViewById(R.id.et_email);
        domain =(EditText)findViewById(R.id.domain);
        password =(EditText)findViewById(R.id.et_password);
        //set listeners

        login.addTextChangedListener(textWatcher);
        password.addTextChangedListener(textWatcher);
        domain.addTextChangedListener(textWatcher);


        checkFieldsForEmptyValues();



        //  port =(EditText)findViewById(R.id.dest);
       Spinner spin = (Spinner) findViewById(R.id.dest);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, users);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       spin.setAdapter(adapter);
        spin.setOnItemSelectedListener(this);

        checkPermissions();








        // Button send = (Button)findViewById(R.id.btn_register);
        Button register = (Button)findViewById(R.id.btn_login);




        //     Intent intent= getIntent();
        //     if(intent.getStringExtra("key") =="20"){
        //      Log.e("javan value",intent.getStringExtra("key"));


        //  }

       /* checkBox1=(CheckBox)findViewById(R.id.checkBox1);
        checkBox2=(CheckBox)findViewById(R.id.checkBox2);
        checkBox3=(CheckBox)findViewById(R.id.checkBox3);
        checkBox4=(CheckBox)findViewById(R.id.checkBox4);*/
        checkBoxer=(CheckBox)findViewById(R.id.checkBoxer);
        checkBoxer.setOnCheckedChangeListener(new Chk_class());

        liftname=(EditText)findViewById(R.id.liftnumber);


        Group group2=(Group)findViewById(R.id.group2); //bind view from xml
        group2.setVisibility(View.GONE); //this will visible all views
        Group group3=(Group)findViewById(R.id.group3); //bind view from xml
        group3.setVisibility(View.GONE); //this will visible all views


        Guideline TOP = (Guideline) findViewById(R.id.TOP);
        Guideline BOTTOM = (Guideline) findViewById(R.id.BOTTOM);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) TOP.getLayoutParams();
        ConstraintLayout.LayoutParams param1 = (ConstraintLayout.LayoutParams) BOTTOM.getLayoutParams();
        params.guidePercent = 0.25f; // 25% // range: 0 <-> 1
        TOP.setLayoutParams(params);
        param1.guidePercent = 0.75f; // 45% // range: 0 <-> 1
        BOTTOM.setLayoutParams(param1);


      //  LinearLayout checkbox = (LinearLayout) findViewById(R.id.checkBoxLayout);
       // LinearLayout reportCall = (LinearLayout) findViewById(R.id.reportCall);
       // TextView checkboxTittle =(TextView) findViewById(R.id.checktitle);



      //  checkbox.setVisibility(View.GONE);
     //   reportCall.setVisibility(View.GONE);
      //  checkboxTittle.setVisibility(View.GONE);


   /*     if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                }
            }
        }
    }
*/




       /* checkBox1.setOnCheckedChangeListener(new Chk_class());
        checkBox2.setOnCheckedChangeListener(new Chk_class());
        checkBox3.setOnCheckedChangeListener(new Chk_class());
        checkBox4.setOnCheckedChangeListener(new Chk_class());
        // checkBoxer.setOnCheckedChangeListener(new Chk_class());
*/
        liftname=(EditText)findViewById(R.id.liftnumber);

        Button button1= (Button)findViewById(R.id.buttonCall);
       // Button button2= (Button)findViewById(R.id.buttonSend);


        button1.setVisibility(View.INVISIBLE);
        register.setOnClickListener(this);
        button1.setOnClickListener(this);
      //  button2.setOnClickListener(this);




        btnScan = (Button)findViewById(R.id.btnScan);
       // tv_qr_readTxt = (TextView) findViewById(R.id.tv_qr_readTxt);


        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                IntentIntegrator integrator = new IntentIntegrator(MainActivity2.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();

            }





        } );

        /*********************************************************************/
        if (app == null) {
            app = new MyApp();
            // Wait for GDB to init, for native debugging only
            if (false &&
                    (getApplicationInfo().flags &
                            ApplicationInfo.FLAG_DEBUGGABLE) != 0)
            {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {}
            }

            app.init(this, getFilesDir().getAbsolutePath());
        }

        if (app.accList.size() == 0) {
            accCfg = new AccountConfig();
            accCfg.setIdUri("sip:localhost");
            accCfg.getNatConfig().setIceEnabled(false);
            accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
            accCfg.getVideoConfig().setAutoShowIncoming(true);
            account = app.addAcc(accCfg);
        } else {
            account = app.accList.get(0);
            accCfg = account.cfg;
        }


        if (receiver == null) {
            receiver = new MyBroadcastReceiver();
            intentFilter = new IntentFilter(
                    ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(receiver, intentFilter);
        }
        /**********************************************************************/







    }






    private  void checkFieldsForEmptyValues(){
        Button btn = (Button) findViewById(R.id.btn_login);

        String s1 = login.getText().toString();
        String s2 = password.getText().toString();
        String s3 = domain.getText().toString();

        if (s1.length() > 0 && s2.length() > 0 && s3.length() > 0) {
            btn.setEnabled(true);
        } else {
            btn.setEnabled(false);
        }
    }

    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.QRCodeBlackColor):getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 350, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }




    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position,long id) {
        //.makeText(getApplicationContext(), "Selected Port: "+users[position] ,Toast.LENGTH_SHORT).show();
        PORTS=users[position].toLowerCase();

    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    { //PORTS="tcp";
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.e("Scan*******", "Cancelled scan");

            } else {
                Log.e("Scan", "Scanned");

                barCode=result.getContents();

                Toast toast = Toast.makeText(this, "Scanned QRCODE : " + result.getContents(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                afterscan();


            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    /**************************************************************************************/
    @Override
    public boolean handleMessage(Message m)
    {
        if (m.what == 0) {

            app.deinit();
            finish();
            Runtime.getRuntime().gc();
            android.os.Process.killProcess(android.os.Process.myPid());

        } else if (m.what == MSG_TYPE.CALL_STATE) {

            CallInfo ci = (CallInfo) m.obj;

            if (currentCall == null || ci == null || ci.getId() != currentCall.getId()) {
                System.out.println("Call state event received, but call info is invalid");
                return true;
            }

            /* Forward the call info to CallActivity */
            if (CallActivity.handler_ != null) {
                Message m2 = Message.obtain(CallActivity.handler_, MSG_TYPE.CALL_STATE, ci);
                m2.sendToTarget();
            }

            if (ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED)
            {
                afterRegistration2();
                CallOpParam prm = new CallOpParam();
                prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
                currentCall.delete();
                    try {
                    //    currentCall.hangup(prm);
                        } catch (Exception e) {
                        System.out.println(e);
                        }




                currentCall = null;
                Log.e("javan value","call ended ");



            }

        } else if (m.what == MSG_TYPE.CALL_MEDIA_STATE) {

            /* Forward the message to CallActivity */
            if (CallActivity.handler_ != null) {
                Message m2 = Message.obtain(CallActivity.handler_,
                        MSG_TYPE.CALL_MEDIA_STATE,
                        null);
                m2.sendToTarget();
            }

        } else if (m.what == MSG_TYPE.BUDDY_STATE) {

            MyBuddy buddy = (MyBuddy) m.obj;
            int idx = account.buddyList.indexOf(buddy);

            /* Update buddy status text, if buddy is valid and
             * the buddy lists in account and UI are sync-ed.
             */
            if (idx >= 0 && account.buddyList.size() == buddyList.size())
            {
                buddyList.get(idx).put("status", buddy.getStatusText());
                buddyListAdapter.notifyDataSetChanged();
                // TODO: selection color/mark is gone after this,
                //       dont know how to return it back.
                //buddyListView.setSelection(buddyListSelectedIdx);
                //buddyListView.performItemClick(buddyListView,
                //				     buddyListSelectedIdx,
                //				     buddyListView.
                //		    getItemIdAtPosition(buddyListSelectedIdx));

                /* Return back Call activity */
                notifyCallState(currentCall);
            }

        } else if (m.what == MSG_TYPE.REG_STATE) {

            String msg_str = (String) m.obj;
            lastRegStatus = msg_str;

        } else if (m.what == MSG_TYPE.INCOMING_CALL) {

            /* Incoming call */
            final MyCall call = (MyCall) m.obj;
            CallOpParam prm = new CallOpParam();

            /* Only one call at anytime */
            if (currentCall != null) {
		/*
		prm.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);
		try {
		call.hangup(prm);
		} catch (Exception e) {}
		*/
                // TODO: set status code
                call.delete();
                return true;
            }

            /* Answer with ringing */
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_RINGING);
            try {
                call.answer(prm);
            } catch (Exception e) {}

            currentCall = call;
            showCallActivity();

        } else if (m.what == MSG_TYPE.CHANGE_NETWORK) {
            app.handleNetworkChange();
        } else {

            /* Message not handled */
            return false;

        }

        return true;
    }



    public void afterscan(){

        TextView header = (TextView) findViewById(R.id.domain2);
        header.setVisibility(View.GONE);


        Group group=(Group)findViewById(R.id.group2); //bind view from xml
        group.setVisibility(View.GONE); //this will visible all views
        Guideline TOP = (Guideline) findViewById(R.id.TOP);
        Group group3=(Group)findViewById(R.id.group3); //bind view from xml
        group3.setVisibility(View.VISIBLE); //this will visible all views
        Guideline BOTTOM = (Guideline) findViewById(R.id.BOTTOM);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) TOP.getLayoutParams();
        ConstraintLayout.LayoutParams param1 = (ConstraintLayout.LayoutParams) BOTTOM.getLayoutParams();
        params.guidePercent = 0.10f; // 25% // range: 0 <-> 1
        TOP.setLayoutParams(params);
        param1.guidePercent = 0.90f; // 45% // range: 0 <-> 1
        BOTTOM.setLayoutParams(param1);


    }


    public static void triggerRebirth(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        context.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }


    @Override
    public void onBackPressed() {


        try {
            account.setRegistration(false);
            account.delete();
           // account=null;
        } catch (Exception e) {
            e.printStackTrace();

        }
      /*  AlarmManager alm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alm.set(AlarmManager.RTC, System.currentTimeMillis() + 3000, PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()), 0));

        //android.os.Process.killProcess(android.os.Process.myPid());
        //finishAndRemoveTask();


        Intent intent = new Intent(this, MainActivity2.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);*/

        Context context = this;

        Intent i = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        System.exit(0);

      //  Intent intent = new Intent(getBaseContext(), MainActivity2.class);

      //  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
      //  startActivity(intent);


    }

        // Fetch the stored data in onResume()
    // Because this is what will be called
    // when the app opens again
    @Override
    protected void onResume() {
        super.onResume();


        // Fetching the stored data
        // from the SharedPreference

        Spinner spin = (Spinner) findViewById(R.id.dest);


        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        String loginsh = sh.getString("login", "");
        String passwordsh = sh.getString("password", "");
        String domainsh = sh.getString("domain", "");

        int spinnerValue = sh.getInt("userChoiceSpinner",-1);
        if(spinnerValue != -1) {
            // set the selected value of the spinner
            spin.setSelection(spinnerValue);
        }

        // Setting the fetched data
        // in the EditTexts
        login.setText(loginsh);
        password.setText(String.valueOf(passwordsh));
        domain.setText(String.valueOf(domainsh));


    }

    // Store the data in the SharedPreference
    // in the onPause() method
    // When the user closes the application
    // onPause() will be called
    // and data will be stored
    @Override
    protected void onPause() {
        super.onPause();




        // Creating a shared pref object
        // with a file name "MySharedPref"
        // in private mode

        Spinner spin = (Spinner) findViewById(R.id.dest);

        int userChoice = spin.getSelectedItemPosition();
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

     myEdit.putInt("userChoiceSpinner",userChoice);

        // write all the data entered by the user in SharedPreference and apply
        myEdit.putString("login", login.getText().toString());
        myEdit.putString("password", password.getText().toString());
        myEdit.putString("domain", domain.getText().toString());
        myEdit.putString("port", PORTS);


        myEdit.apply();




    }




    @Override
    protected void onStop() {

        super.onStop();
       // android.os.Process.killProcess(android.os.Process.myPid());


    }



 public void showSnackbar(String message){
     final ConstraintLayout constraintLayout = findViewById(R.id.contentFragments);

     Snackbar snackbar=Snackbar.make(constraintLayout,"THIS IS SNAACKBAR",Snackbar.LENGTH_INDEFINITE);
     snackbar.show();


 }


    public void dlgAccountSetting()
    {


        subdomain = DOMAIN.substring(DOMAIN.indexOf(".")+1);
        subdomain.trim();


        String username = USERNAME;//"90701871890";//"90762070083"; //etUser.getText().toString();
        String acc_id = "<sip:"+username+"@"+subdomain+";transport="+PORTS+ ">"; //etId.getText().toString();
        String registrar = "<sip:"+DOMAIN+";transport="+PORTS+ ">"; //etReg.getText().toString();
        String proxy = "<sip:"+DOMAIN+";transport="+PORTS+ ">";  //etProxy.getText().toString();
        String password = PASSWORD;//"p3KB7sgh9c8c"; //etPass.getText().toString()9e4t3gh7ZPqC for 90763167382
        accCfg.setIdUri(acc_id);
        accCfg.getRegConfig().setRegistrarUri(registrar);
        AuthCredInfoVector creds = accCfg.getSipConfig().
                getAuthCreds();
        creds.clear();

        //configure an accountconfig

        if (username.length() != 0) {
            creds.add(new AuthCredInfo("Digest", "*", username, 0,
                    password));
        }
        StringVector proxies = accCfg.getSipConfig().getProxies();
        proxies.clear();
        if (proxy.length() != 0) {
            proxies.add(proxy);
        }

        /* Enable ICE */
        //   accCfg.getNatConfig().setIceDisable(true);

        /* Finally */
        lastRegStatus = "";
        try {
            account.modify(accCfg);
        } catch (Exception e) {}


    }










    @Override
    public void onClick(View v)
    {


        if (v.getId() == R.id.buttonCall){

            dlgAccountSetting();
           makeCall(DESTINATION,barCode,"");
            Log.e("javan barcode ",String.valueOf(barCode));



        }

        else if (v.getId() == R.id.btn_login){

         CallInfo ci;


            USERNAME  =login.getText().toString();//"242424";
            // login.getText().toString();
            PASSWORD =  password.getText().toString();// "NpgUqVnaa2fp";//
            DOMAIN =   domain.getText().toString();// "ucp-fs-dev.serv24.com";//
            DESTINATION="parrot";




        dlgAccountSetting();
        Log.e("javan barcode ",String.valueOf(barCode));



    }


    }




    private void checkPermissions()
    {
        if (Build.VERSION.SDK_INT >= 23)
        {
            //dangerous permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.BODY_SENSORS,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_DANGEROUS_PERMISSIONS);
            }
        }
    }

    public static void wait(int ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }


    public void  afterRegistration2(){

        checkBoxer=(CheckBox)findViewById(R.id.checkBoxer);

        liftname=(EditText)findViewById(R.id.liftnumber);
        liftname.setText("");
         checkBoxer.setChecked(false); //to uncheck



        TextView header = (TextView) findViewById(R.id.domain2);
        header.setText("Enter Lift Details");
        header.setTextSize(28);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.bigshot_one);
        header.setTypeface(typeface);
        Group group3=(Group)findViewById(R.id.group3); //bind view from xml
        group3.setVisibility(View.GONE); //this will visible


        Group group=(Group)findViewById(R.id.group); //bind view from xml
        group.setVisibility(View.GONE); //this will visible all views
        Group group2=(Group)findViewById(R.id.group2); //bind view from xml
        group2.setVisibility(View.VISIBLE); //this will visible all views


        Guideline TOP = (Guideline) findViewById(R.id.TOP);
        Guideline BOTTOM = (Guideline) findViewById(R.id.BOTTOM);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) TOP.getLayoutParams();
        ConstraintLayout.LayoutParams param1 = (ConstraintLayout.LayoutParams) BOTTOM.getLayoutParams();
        params.guidePercent = 0.25f; // 25% // range: 0 <-> 1
        TOP.setLayoutParams(params);
        param1.guidePercent = 0.75f; // 45% // range: 0 <-> 1
        BOTTOM.setLayoutParams(param1);


    }


        public void  afterRegistration(){

      TextView header = (TextView) findViewById(R.id.domain2);
      header.setText("Enter Lift Details");
      header.setTextSize(28);
        Typeface typeface = ResourcesCompat.getFont(this, R.font.bigshot_one);
        header.setTypeface(typeface);
            Group group3=(Group)findViewById(R.id.group3); //bind view from xml
            group3.setVisibility(View.GONE); //this will visible


        Group group=(Group)findViewById(R.id.group); //bind view from xml
        group.setVisibility(View.GONE); //this will visible all views
        Group group2=(Group)findViewById(R.id.group2); //bind view from xml
        group2.setVisibility(View.VISIBLE); //this will visible all views


        Guideline TOP = (Guideline) findViewById(R.id.TOP);
        Guideline BOTTOM = (Guideline) findViewById(R.id.BOTTOM);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) TOP.getLayoutParams();
        ConstraintLayout.LayoutParams param1 = (ConstraintLayout.LayoutParams) BOTTOM.getLayoutParams();
        params.guidePercent = 0.15f; // 25% // range: 0 <-> 1
        TOP.setLayoutParams(params);
        param1.guidePercent = 0.75f; // 45% // range: 0 <-> 1
        BOTTOM.setLayoutParams(param1);
        Toast.makeText(MainActivity2.this, "Successfully registered  ", Toast.LENGTH_SHORT).show();


    }

    public void showToast(final String msg) {
        Handler h = new Handler( getApplicationContext().getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                Toast myToast = Toast.makeText( getApplicationContext(), msg, Toast.LENGTH_SHORT);
                myToast.show();
            }
        });
    }

    public void connected(){

       // Toast.makeText(MainActivity2.this, "Successfully registered  ", Toast.LENGTH_SHORT).show();

    }
    public void notconnected(String msg){

        Toast.makeText(MainActivity2.this, msg, Toast.LENGTH_SHORT).show();

    }







    public void makeCall(String dialno,String barcode,String error)
    {


        /* Only one call at anytime */
        if (currentCall != null) {
            return;
        }



        MyCall call = new MyCall(account, -1);
        CallOpParam prm = new CallOpParam(true);
        prm.setOptions(pjsua_call_flag.PJSUA_CALL_INCLUDE_DISABLED_MEDIA); //disabling video
        CallSetting opt = prm.getOpt();

        opt.setVideoCount(0);

        try {
            SipHeader sipHeader = new SipHeader();
            sipHeader.setHName("X-UCP-Parrot-EqId");
            sipHeader.setHValue(barCode);
            SipHeaderVector sipHeaderVector = new SipHeaderVector();
            sipHeaderVector.add(sipHeader);
            SipTxOption sipTxOption = new SipTxOption();
            sipTxOption.setHeaders(sipHeaderVector);
            prm.setTxOption(sipTxOption);



            call.makeCall("<sip:"+dialno+"@" +subdomain+">" , prm);
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            am.setSpeakerphoneOn(true);
        } catch (Exception e) {
            call.delete();
            return;
        }

        currentCall = call;
        showCallActivity();
        wait(3500);
        afterRegistration2();
    }
    /*
     * === MyAppObserver ===
     *
     * As we cannot do UI from worker thread, the callbacks mostly just send
     * a message to UI/main thread.
     */

    public void notifyIncomingCall(MyCall call)
    {
        Message m = Message.obtain(handler, MSG_TYPE.INCOMING_CALL, call);
        m.sendToTarget();
    }
    @Override

    public void notifyRegState(int code, String reason,
                               long expiration)
    {

        if (expiration == 0)
            msg_str += "Unregistration";
        else
            msg_str += "Registration";

        if (code/100 == 2) {
            msg_str += " successful";

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    afterRegistration();
                }
            });


        }
        else
          //  msg_str += " failed: " + reason;
        msg_str=null;
        msg_str = "sip status :" + reason;
        if(reason=="OK")
            afterRegistration();


        if(msg_str=="Connection established")
        Log.e("javan barcodesss ",String.valueOf(msg_str));


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notconnected(msg_str);
            }
        });
       //


        Message m = Message.obtain(handler, MSG_TYPE.REG_STATE, msg_str);
        m.sendToTarget();




    }

    public void notifyCallState(MyCall call)
    {
        if (currentCall == null || call.getId() != currentCall.getId())
            return;

        CallInfo ci = null;
        try {
            ci = call.getInfo();
        } catch (Exception e) {}

        if (ci == null){

            return ;
        }




        Message m = Message.obtain(handler, MSG_TYPE.CALL_STATE, ci);
        m.sendToTarget();
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    public void notifyCallMediaState(MyCall call)
    {
        Message m = Message.obtain(handler, MSG_TYPE.CALL_MEDIA_STATE, null);
        m.sendToTarget();
    }

    public void notifyBuddyState(MyBuddy buddy)
    {
        Message m = Message.obtain(handler, MSG_TYPE.BUDDY_STATE, buddy);
        m.sendToTarget();
    }

    public void notifyChangeNetwork()
    {
        Message m = Message.obtain(handler, MSG_TYPE.CHANGE_NETWORK, null);
        m.sendToTarget();
    }
    /*************************************************************************************/



    class Chk_class implements CompoundButton.OnCheckedChangeListener{


        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {





            if(checkBoxer.isChecked()&&checkBoxer.getId()==buttonView.getId()){
                barCode=  liftname.getText().toString();
                closeKeyboard();
                afterscan();

            }



        }
    }








}