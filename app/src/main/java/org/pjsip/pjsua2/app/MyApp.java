/* $Id: MyApp.java 5361 2016-06-28 14:32:08Z nanang $ */
/*
 * Copyright (C) 2013 Teluu Inc. (http://www.teluu.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pjsip.pjsua2.app;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import org.pjsip.pjsua2.*;


/* Interface to separate UI & engine a bit better */
interface MyAppObserver
{
	abstract void notifyRegState(int code, String reason, long expiration);
	abstract void notifyIncomingCall(MyCall call);
	abstract void notifyCallState(MyCall call);
	abstract void notifyCallMediaState(MyCall call);
	abstract void notifyBuddyState(MyBuddy buddy);
	abstract void notifyChangeNetwork();
}


class MyLogWriter extends LogWriter
{
	@Override
	public void write(LogEntry entry)
	{
		System.out.println(entry.getMsg());
	}
}


class MyCall extends Call
{
	public VideoWindow vidWin;
	public VideoPreview vidPrev;

	MyCall(MyAccount acc, int call_id)
	{
		super(acc, call_id);
		vidWin = null;
	}




	@Override
	public void onCallTsxState(OnCallTsxStateParam prm) {



		Log.e("james", "onCallTsxState");
		SipEventBody eventBody = prm.getE().getBody();
		Log.e("james", String.valueOf(eventBody.getTxMsg()));
		Log.e("james", String.valueOf(eventBody.getRxMsg()));
		Log.e("james", String.valueOf(eventBody.getClass().getName()));
		Log.e("james", String.valueOf(eventBody.getTimer()));
		Log.e("james", String.valueOf(eventBody.getUser()));

   //store entire header file and


	if (eventBody.getTsxState().getType() == (pjsip_event_id_e.PJSIP_EVENT_RX_MSG)) {
			SipRxData rdata = eventBody.getTsxState().getSrc().getRdata();
			String messageBuffer = rdata.getWholeMsg();
			Log.e("bingo", String.valueOf(messageBuffer));

		if(messageBuffer.contains("Connection established")){
			Log.e("bingo", "bingo established");

			try {


				Handler mHandler = new Handler(Looper.getMainLooper());
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						CallActivity call= new CallActivity();
						call.buttonHangup2.setBackgroundResource(R.drawable.btn_green);
						call.buttonHangup.setBackgroundResource(R.drawable.btn_red);
						call.buttonHangup.setText("Hangup no ok");
						call.buttonHangup2.setVisibility(View.VISIBLE);



					}
				});




			}catch (Exception e) {
			}


		}
		super.onCallTsxState(prm);
	}
	}

	@Override
	public void onCallState(OnCallStateParam prm)
	{
		try {
			CallInfo ci = getInfo();
			if (ci.getState() ==
					pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED)
			{
				MyApp.ep.utilLogWrite(3, "MyCall", this.dump(true, ""));
			}
		} catch (Exception e) {
		}

		// Should not delete this call instance (self) in this context,
		// so the observer should manage this call instance deletion
		// out of this callback context.
		MyApp.observer.notifyCallState(this);
	}

	@Override
	public void onCallMediaState(OnCallMediaStateParam prm)
	{
		CallInfo ci;
		try {
			ci = getInfo();
		} catch (Exception e) {
			return;
		}

		CallMediaInfoVector cmiv = ci.getMedia();

		for (int i = 0; i < cmiv.size(); i++) {
			CallMediaInfo cmi = cmiv.get(i);
			if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO &&
					(cmi.getStatus() ==
							pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE ||
							cmi.getStatus() ==
									pjsua_call_media_status.PJSUA_CALL_MEDIA_REMOTE_HOLD))
			{
				// connect ports
				try {
					AudioMedia am = getAudioMedia(i);
					MyApp.ep.audDevManager().getCaptureDevMedia().
							startTransmit(am);
					am.startTransmit(MyApp.ep.audDevManager().
							getPlaybackDevMedia());
				} catch (Exception e) {
					System.out.println("Failed connecting media ports" +
							e.getMessage());
					continue;
				}
			} else if (cmi.getType() == pjmedia_type.PJMEDIA_TYPE_VIDEO &&
					cmi.getStatus() ==
							pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE &&
					cmi.getVideoIncomingWindowId() != pjsua2.INVALID_ID)
			{
				vidWin = new VideoWindow(cmi.getVideoIncomingWindowId());
				vidPrev = new VideoPreview(cmi.getVideoCapDev());
			}
		}

		MyApp.observer.notifyCallMediaState(this);
	}
}


class MyAccount extends Account
{
	public ArrayList<MyBuddy> buddyList = new ArrayList<MyBuddy>();
	public AccountConfig cfg;

	MyAccount(AccountConfig config)
	{
		super();
		cfg = config;
	}

	public MyBuddy addBuddy(BuddyConfig bud_cfg)
	{
		/* Create Buddy */
		MyBuddy bud = new MyBuddy(bud_cfg);
		try {
			bud.create(this, bud_cfg);
		} catch (Exception e) {
			bud.delete();
			bud = null;
		}

		if (bud != null) {
			buddyList.add(bud);
			if (bud_cfg.getSubscribe())
				try {
					bud.subscribePresence(true);
				} catch (Exception e) {}
		}

		return bud;
	}

	public void delBuddy(MyBuddy buddy)
	{
		buddyList.remove(buddy);
		buddy.delete();
	}

	public void delBuddy(int index)
	{
		MyBuddy bud = buddyList.get(index);
		buddyList.remove(index);
		bud.delete();
	}

	@Override
	public void onRegState(OnRegStateParam prm)
	{
		MyApp.observer.notifyRegState(prm.getCode(), prm.getReason(),
				prm.getExpiration());

		if(prm.getReason().equals("OK")) {

			Log.e("javan", "EASY BOSS WE ARE ONLINE ");
			MainActivity2.Reg=007;

			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					MainActivity2 main=new MainActivity2();
				//	main.showSnackbar("EASY BOSE WE ARE ONLINE ");
				}
			});


		}


		if(prm.getReason().equals("Connection established")) {

			Log.e("javan", "james bond aya hai  ");

			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					//	MainActivity2 main=new MainActivity2();
					//main.connected();
				}
			});



			//




		}


		else {
		//	Log.e("javan", "registration failed now .... ");


			Log.e("javan", prm.getReason());
		}

	}



	@Override
	public void onIncomingCall(OnIncomingCallParam prm)
	{
		System.out.println("======== Incoming call ======== ");
		MyCall call = new MyCall(this, prm.getCallId());
		MyApp.observer.notifyIncomingCall(call);
	}

	@Override
	public void onInstantMessage(OnInstantMessageParam prm)
	{


		Log.e("javannew", String.valueOf(prm.getRdata()));
		Log.e("javannew", String.valueOf(prm.getMsgBody()));
		Log.e("javannew", String.valueOf(prm.getRdata()));
		Log.e("javannew", String.valueOf(prm.getRdata()));
		Log.e("javannew", String.valueOf(prm.getRdata()));
		Log.e("javannew", String.valueOf(prm.getRdata()));
		Log.e("javannew", String.valueOf(prm.getRdata()));



		System.out.println("======== Incoming pager ======== ");
		System.out.println("From     : " + prm.getFromUri());

		System.out.println("To       : " + prm.getToUri());
		System.out.println("Contact  : " + prm.getContactUri());
		System.out.println("Mimetype : " + prm.getContentType());
		System.out.println("Body     : " + prm.getMsgBody());
		super.onInstantMessage(prm);

	}
}


class MyBuddy extends Buddy
{
	public BuddyConfig cfg;

	MyBuddy(BuddyConfig config)
	{
		super();
		cfg = config;
	}

	String getStatusText()
	{
		BuddyInfo bi;

		try {
			bi = getInfo();
		} catch (Exception e) {
			return "?";
		}

		String status = "";
		if (bi.getSubState() == pjsip_evsub_state.PJSIP_EVSUB_STATE_ACTIVE) {
			if (bi.getPresStatus().getStatus() ==
					pjsua_buddy_status.PJSUA_BUDDY_STATUS_ONLINE)
			{
				status = bi.getPresStatus().getStatusText();
				if (status == null || status.length()==0) {
					status = "Online";
				}
			} else if (bi.getPresStatus().getStatus() ==
					pjsua_buddy_status.PJSUA_BUDDY_STATUS_OFFLINE)
			{
				status = "Offline";
			} else {
				status = "Unknown";
			}
		}
		return status;
	}

	@Override
	public void onBuddyState()
	{
		MyApp.observer.notifyBuddyState(this);
	}

}




class MyAccountConfig
{
	public AccountConfig accCfg = new AccountConfig();
	public ArrayList<BuddyConfig> buddyCfgs = new ArrayList<BuddyConfig>();

	public void readObject(ContainerNode node)
	{
		try {
			ContainerNode acc_node = node.readContainer("Account");
			accCfg.readObject(acc_node);
			ContainerNode buddies_node = acc_node.readArray("buddies");
			buddyCfgs.clear();
			while (buddies_node.hasUnread()) {
				BuddyConfig bud_cfg = new BuddyConfig();
				bud_cfg.readObject(buddies_node);
				buddyCfgs.add(bud_cfg);
			}
		} catch (Exception e) {}
	}

	public void writeObject(ContainerNode node)
	{
		try {
			ContainerNode acc_node = node.writeNewContainer("Account");
			accCfg.writeObject(acc_node);
			ContainerNode buddies_node = acc_node.writeNewArray("buddies");
			for (int j = 0; j < buddyCfgs.size(); j++) {
				buddyCfgs.get(j).writeObject(buddies_node);
			}
		} catch (Exception e) {}
	}
}


class MyApp {
	static {
		try{
		//	System.loadLibrary("openh264");
			// Ticket #1937: libyuv is now included as static lib
			//System.loadLibrary("yuv");
		} catch (UnsatisfiedLinkError e) {
			System.out.println("UnsatisfiedLinkError: " + e.getMessage());
			System.out.println("This could be safely ignored if you " +
					"don't need video.");
		}
		System.loadLibrary("pjsua2");
		System.out.println("Library loaded");
	}
	// endpoint class instantiate
	public static Endpoint ep = new Endpoint();
	public static MyAppObserver observer;
	public ArrayList<MyAccount> accList = new ArrayList<MyAccount>();

	private ArrayList<MyAccountConfig> accCfgs =
			new ArrayList<MyAccountConfig>();
	//epconfig class provide endpoint configuration ...which does viewl config
	private EpConfig epConfig = new EpConfig();
	private TransportConfig sipTpConfig = new TransportConfig();
	private String appDir;

	/* Maintain reference to log writer to avoid premature cleanup by GC */
	private MyLogWriter logWriter;

	private final String configName = "pjsua2.json";
	private final int SIP_PORT  = 6000;
	private final int LOG_LEVEL = 4;

	public void init(MyAppObserver obs, String app_dir)
	{
		init(obs, app_dir, false);
	}

	public void init(MyAppObserver obs, String app_dir,
					 boolean own_worker_thread)
	{
		observer = obs;
		appDir = app_dir;

		/* Create endpoint  */
		try {
			ep.libCreate();
		} catch (Exception e) {
			return;
		}


		/* Load config */
		String configPath = appDir + "/" + configName;
		File f = new File(configPath);
		if (f.exists()) {
			loadConfig(configPath);
		} else {
			/* Set 'default' values */
			sipTpConfig.setPort(SIP_PORT);
		}

		/* Override log level setting */
		epConfig.getLogConfig().setLevel(LOG_LEVEL);
		epConfig.getLogConfig().setConsoleLevel(LOG_LEVEL);

		/* Set log config. */
		LogConfig log_cfg = epConfig.getLogConfig();
		logWriter = new MyLogWriter();
		log_cfg.setWriter(logWriter);
		log_cfg.setDecor(log_cfg.getDecor() &
				~(pj_log_decoration.PJ_LOG_HAS_CR |
						pj_log_decoration.PJ_LOG_HAS_NEWLINE));

		/* Write log to file (just uncomment whenever needed) */
		//String log_path = android.os.Environment.getExternalStorageDirectory().toString();
		//log_cfg.setFilename(log_path + "/pjsip.log");

		/* Set ua config. */
		UaConfig ua_cfg = epConfig.getUaConfig();
		ua_cfg.setUserAgent("Pjsua2 Android " + ep.libVersion().getFull());

		/* STUN server. */
		//StringVector stun_servers = new StringVector();
		//stun_servers.add("stun.pjsip.org");
		//ua_cfg.setStunServer(stun_servers);

		/* No worker thread */
		if (own_worker_thread) {
			ua_cfg.setThreadCnt(0);
			ua_cfg.setMainThreadOnly(true);
		}

		/* Init endpoint */
		try {
			ep.libInit(epConfig);
		} catch (Exception e) {
			return;
		}

		/* Create transports. */
		try {
			ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP,
					sipTpConfig);
		} catch (Exception e) {
			System.out.println(e);
		}

		try {
			ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP,
					sipTpConfig);
		} catch (Exception e) {
			System.out.println(e);
		}

		try {
			sipTpConfig.setPort(SIP_PORT+1);
			ep.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TLS,
					sipTpConfig);
		} catch (Exception e) {
			System.out.println(e);
		}

		/* Set SIP port back to default for JSON saved config */
		sipTpConfig.setPort(SIP_PORT);

		/* Create accounts.  007*/



		for (int i = 0; i < accCfgs.size(); i++) {
			MyAccountConfig my_cfg = accCfgs.get(i);

			/* Customize account config */
			my_cfg.accCfg.getNatConfig().setIceEnabled(true);
			my_cfg.accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
			my_cfg.accCfg.getVideoConfig().setAutoShowIncoming(true);

			/* Enable SRTP optional mode and without requiring SIP TLS transport */
			my_cfg.accCfg.getMediaConfig().setSrtpUse(pjmedia_srtp_use.PJMEDIA_SRTP_OPTIONAL);
			my_cfg.accCfg.getMediaConfig().setSrtpSecureSignaling(0);

			MyAccount acc = addAcc(my_cfg.accCfg);
			if (acc == null)
				continue;

			/* Add Buddies */
			for (int j = 0; j < my_cfg.buddyCfgs.size(); j++) {
				BuddyConfig bud_cfg = my_cfg.buddyCfgs.get(j);
				acc.addBuddy(bud_cfg);
			}
		}

		/* Start. */
		try {
			ep.libStart();

			setCodecPriorities();
		} catch (Exception e) {
			return;
		}
	}

	public MyAccount addAcc(AccountConfig cfg)
	{
		MyAccount acc = new MyAccount(cfg);
		try {
			acc.create(cfg);   // account create
		} catch (Exception e) {
			System.out.println(e);
			acc = null;
			return null;
		}

		accList.add(acc);
		return acc;
	}

	public void delAcc(MyAccount acc)
	{
		accList.remove(acc);
	}

	private void loadConfig(String filename)
	{
		JsonDocument json = new JsonDocument();

		try {
			/* Load file */
			json.loadFile(filename);
			ContainerNode root = json.getRootContainer();

			/* Read endpoint config */
			epConfig.readObject(root);

			/* Read transport config */
			ContainerNode tp_node = root.readContainer("SipTransport");
			sipTpConfig.readObject(tp_node);

			/* Read account configs */
			accCfgs.clear();
			ContainerNode accs_node = root.readArray("accounts");
			while (accs_node.hasUnread()) {
				MyAccountConfig acc_cfg = new MyAccountConfig();
				acc_cfg.readObject(accs_node);
				accCfgs.add(acc_cfg);
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		/* Force delete json now, as I found that Java somehow destroys it
		 * after lib has been destroyed and from non-registered thread.
		 */
		json.delete();
	}

	private void buildAccConfigs()
	{
		/* Sync accCfgs from accList */
		accCfgs.clear();
		for (int i = 0; i < accList.size(); i++) {
			MyAccount acc = accList.get(i);
			MyAccountConfig my_acc_cfg = new MyAccountConfig();
			my_acc_cfg.accCfg = acc.cfg;

			my_acc_cfg.buddyCfgs.clear();
			for (int j = 0; j < acc.buddyList.size(); j++) {
				MyBuddy bud = acc.buddyList.get(j);
				my_acc_cfg.buddyCfgs.add(bud.cfg);
			}

			accCfgs.add(my_acc_cfg);
		}
	}

	private void setCodecPriorities() {

		if (ep != null) {
			try {
				CodecInfoVector2 codecInfoVector = ep.codecEnum2();
				for (int i = 0; i < codecInfoVector.size(); i++) {
					CodecInfo codecInfo = codecInfoVector.get(i);
					String codecId = codecInfo.getCodecId();
					short codecPriority = 128;//use higher number for making preferred codec first.
					short disableCodecPriority = 0;//use 0 to disable codec in sdp
					System.out.println("Codec info now is: "+ codecId);

					if (codecInfo.getCodecId().contains("PCMA/8000/1")) // codec priority
						ep.codecSetPriority(codecInfo.getCodecId(), (short) 128);
					else if (codecInfo.getCodecId().contains("PCMU/8000/1")) // codec priority
						ep.codecSetPriority(codecInfo.getCodecId(), (short) 128);

					else
						ep.codecSetPriority(codecInfo.getCodecId(), (short) 0);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	private void saveConfig(String filename)
	{
		JsonDocument json = new JsonDocument();

		try {
			/* Write endpoint config */
			json.writeObject(epConfig);

			/* Write transport config */
			ContainerNode tp_node = json.writeNewContainer("SipTransport");
			sipTpConfig.writeObject(tp_node);

			/* Write account configs */
			buildAccConfigs();
			ContainerNode accs_node = json.writeNewArray("accounts");
			for (int i = 0; i < accCfgs.size(); i++) {
				accCfgs.get(i).writeObject(accs_node);
			}

			/* Save file */
			json.saveFile(filename);
		} catch (Exception e) {}

		/* Force delete json now, as I found that Java somehow destroys it
		 * after lib has been destroyed and from non-registered thread.
		 */
		json.delete();
	}

	public void handleNetworkChange()
	{
		try{
			System.out.println("Network change detected");
			IpChangeParam changeParam = new IpChangeParam();
			ep.handleIpChange(changeParam);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void deinit()
	{
		String configPath = appDir + "/" + configName;
		saveConfig(configPath);

		/* Try force GC to avoid late destroy of PJ objects as they should be
		 * deleted before lib is destroyed.
		 */
		Runtime.getRuntime().gc();

		/* Shutdown pjsua. Note that Endpoint destructor will also invoke
		 * libDestroy(), so this will be a test of double libDestroy().
		 */
		try {
			ep.libDestroy();
		} catch (Exception e) {}

		/* Force delete Endpoint here, to avoid deletion from a non-
		 * registered thread (by GC?).
		 */
		ep.delete();
		ep = null;
	}
}
