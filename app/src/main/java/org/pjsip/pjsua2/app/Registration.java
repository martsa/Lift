package org.pjsip.pjsua2.app;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity implements Handler.Callback, MyAppObserver,View.OnClickListener
    {

        public static MyApp app = null;
        public static MyCall currentCall = null;
        public static MyAccount account = null;
        public static AccountConfig accCfg=null;
        public static Registration.MyBroadcastReceiver receiver = null;
        public static IntentFilter intentFilter = null;

        private ListView buddyListView;
        private SimpleAdapter buddyListAdapter;
        private int buddyListSelectedIdx = -1;
        ArrayList<Map<String, String>> buddyList;
        private String lastRegStatus = "";


        private final Handler handler = new Handler(this);
        public class MSG_TYPE {
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
                        ((ConnectivityManager) context.getSystemService(
                                Context.CONNECTIVITY_SERVICE));

                NetworkInfo net_info = connectivity_mgr.getActiveNetworkInfo();
                if (net_info != null && net_info.isConnectedOrConnecting() &&
                        !conn_name.equalsIgnoreCase("")) {
                    String new_con = net_info.getExtraInfo();
                    if (new_con != null && !new_con.equalsIgnoreCase(conn_name))
                        network_changed = true;

                    conn_name = (new_con == null) ? "" : new_con;
                } else {
                    if (conn_name.equalsIgnoreCase(""))
                        conn_name = net_info.getExtraInfo();
                }
                return network_changed;
            }
        }

        private HashMap<String, String> putData (String uri, String status)
        {
            HashMap<String, String> item = new HashMap<String, String>();
            item.put("uri", uri);
            item.put("status", status);
            return item;
        }

        private void showCallActivity ()
        {
            Intent intent = new Intent(this, CallActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }


        @Override
        protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        if (app == null) {
            app = new MyApp();
            // Wait for GDB to init, for native debugging only
            if (false &&
                    (getApplicationInfo().flags &
                            ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
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
            receiver = new Registration.MyBroadcastReceiver();
            intentFilter = new IntentFilter(
                    ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(receiver, intentFilter);
        }


    }



        @Override
        public boolean handleMessage(Message m)
        {
            if (m.what == 0) {

                app.deinit();
                finish();
                Runtime.getRuntime().gc();
                android.os.Process.killProcess(android.os.Process.myPid());

            } else if (m.what == Registration.MSG_TYPE.CALL_STATE) {

                CallInfo ci = (CallInfo) m.obj;

                if (currentCall == null || ci == null || ci.getId() != currentCall.getId()) {
                    System.out.println("Call state event received, but call info is invalid");
                    return true;
                }

                /* Forward the call info to CallActivity */
                if (CallActivity.handler_ != null) {
                    Message m2 = Message.obtain(CallActivity.handler_, Registration.MSG_TYPE.CALL_STATE, ci);
                    m2.sendToTarget();
                }

                if (ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED)
                {
                    currentCall.delete();
                    currentCall = null;
                }

            } else if (m.what == Registration.MSG_TYPE.CALL_MEDIA_STATE) {

                /* Forward the message to CallActivity */
                if (CallActivity.handler_ != null) {
                    Message m2 = Message.obtain(CallActivity.handler_,
                            Registration.MSG_TYPE.CALL_MEDIA_STATE,
                            null);
                    m2.sendToTarget();
                }

            } else if (m.what == Registration.MSG_TYPE.BUDDY_STATE) {

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

            } else if (m.what == Registration.MSG_TYPE.REG_STATE) {

                String msg_str = (String) m.obj;
                lastRegStatus = msg_str;

            } else if (m.what == Registration.MSG_TYPE.INCOMING_CALL) {

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

            } else if (m.what == Registration.MSG_TYPE.CHANGE_NETWORK) {
                app.handleNetworkChange();
            } else {

                /* Message not handled */
                return false;

            }

            return true;
        }


        public void dlgAccountSetting()
        {



            String username = "90701871890";//"90762070083"; //etUser.getText().toString();
            String acc_id = "sip:"+username +"@peoplefone.ch;transport=tls"; //etId.getText().toString();
            String registrar = "sip:sips.peoplefone.ch;transport=tls";//etReg.getText().toString();
            String proxy = "sip:sips.peoplefone.ch;transport=tls"; //etProxy.getText().toString();
            String password = "p3KB7sgh9c8c"; //etPass.getText().toString()9e4t3gh7ZPqC for 90763167382
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

            accCfg.getNatConfig().setIceEnabled(false);


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
                String name = "lobsang";
                Toast.makeText(Registration.this,name,Toast.LENGTH_SHORT).show();
                makeCall("0041764193638");

            }
            else {


                String name = "james";
                Toast.makeText(Registration.this, name, Toast.LENGTH_SHORT).show();
                dlgAccountSetting();
            }
        }

        public void callnow(){
        Log.e("javan","we are offline and service are limited ");

    }


        public void makeCall(String dialno)
        {


            /* Only one call at anytime */
            if (currentCall != null) {
                return;
            }



            MyCall call = new MyCall(account, -1);
            CallOpParam prm = new CallOpParam(true);

            try {
                call.makeCall("sip:"+dialno+"@peoplefone.ch", prm);
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                am.setSpeakerphoneOn(true);
            } catch (Exception e) {
                call.delete();
                return;
            }

            currentCall = call;
            showCallActivity();
        }
        /*
         * === MyAppObserver ===
         *
         * As we cannot do UI from worker thread, the callbacks mostly just send
         * a message to UI/main thread.
         */

        public void notifyIncomingCall(MyCall call)
        {
            Message m = Message.obtain(handler, Registration.MSG_TYPE.INCOMING_CALL, call);
            m.sendToTarget();
        }

        public void notifyRegState(int code, String reason,
        long expiration)
        {
            String msg_str = "";
            if (expiration == 0)
                msg_str += "Unregistration";
            else
                msg_str += "Registration";

            if (code/100 == 2)
                msg_str += " successful";
            else
                msg_str += " failed: " + reason;

            Message m = Message.obtain(handler, Registration.MSG_TYPE.REG_STATE, msg_str);
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

            if (ci == null)
                return;

            Message m = Message.obtain(handler, Registration.MSG_TYPE.CALL_STATE, ci);
            m.sendToTarget();
        }

        public void notifyCallMediaState(MyCall call)
        {
            Message m = Message.obtain(handler, Registration.MSG_TYPE.CALL_MEDIA_STATE, null);
            m.sendToTarget();
        }

        public void notifyBuddyState(MyBuddy buddy)
        {
            Message m = Message.obtain(handler, Registration.MSG_TYPE.BUDDY_STATE, buddy);
            m.sendToTarget();
        }

        public void notifyChangeNetwork()
        {
            Message m = Message.obtain(handler, Registration.MSG_TYPE.CHANGE_NETWORK, null);
            m.sendToTarget();
        }


    }