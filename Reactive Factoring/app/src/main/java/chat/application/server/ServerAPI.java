package chat.application.server;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.ArrayList;

import javax.crypto.SecretKey;

import chat.application.MainActivity;
import chat.application.MyAppication;
import chat.application.activity.LoginActivity;
import chat.application.model.Contact;

/**
 * Created by kbaldor on 7/4/16.
 */
public class ServerAPI {
    public final String API_VERSION = "0.4.0";
    private static String LOG       = "ServerAPI";
    public static boolean returnValue;
    public static boolean userRegistered;

    private static ServerAPI ourInstance;

    PublicKey serverKey=null;

    RequestQueue commandQueue;
    RequestQueue pseudoPushQueue;

    Crypto myCrypto;

    public static ServerAPI getInstance(Context context, Crypto crypto) {

        if(ourInstance==null){
            ourInstance = new ServerAPI(context);
            ourInstance.myCrypto = crypto;
        }
        return ourInstance;
    }



    private ServerAPI(Context context) {
        commandQueue = Volley.newRequestQueue(context);
        pseudoPushQueue = Volley.newRequestQueue(context);
    }

    private String myServerName = "SERVER_NAME_NOT_SPECIFIED";
    private String myServerPort = "25666";

    private String makeURL(String... args){
        return "http://"+myServerName+":"+myServerPort+"/"+ TextUtils.join("/",args);
    }

    private void getServerAddress(final String servername){
        (new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Log.d(LOG,"Address is: "+ InetAddress.getByName(servername).getHostAddress());
                    getStringCommand(makeURL("get-key"),
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String key) {
                                    serverKey = Crypto.getPublicKeyFromString(key);
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d(LOG,"Couldn't get key",error);
                                }
                            });
                    //checkAPIVersion();

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    public void setServerPort(final String serverPort){
        myServerPort = serverPort;
    }

    public void setServerName(final String serverName){

        myServerName = serverName;
        getServerAddress(serverName);
    }

    public boolean getAPI() {
        checkAPIVersion();
        Log.d("getAPI","val "+ServerAPI.returnValue);
        return ServerAPI.returnValue;
    }

    public void checkAPIVersion(){
        //boolean returnValue=false;

        getStringCommand(makeURL("api-version"),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if(s.equals(API_VERSION)) {
                            //Log.d(LOG,"API version "+s);
                            ServerAPI.returnValue = true;
                            //Log.d(LOG,"check value "+ServerAPI.returnValue);
                            sendGoodAPIVersion();
                        } else {
                            ServerAPI.returnValue = false;
                            sendBadAPIVersion();
                        }

                        //Log.d(LOG,"check value "+returnValue);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        sendCommandFailed("checkAPIVersion",error);
                        ServerAPI.returnValue = false;
                    }
                });
        //return returnValue;
    }

    public void register(final String username, String image, String publicKey) {
        putJSONCommand(makeURL("register"), keyValuePairs("username",username,
                                                          "image",image,
                                                          "public-key",publicKey),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        handleRegisterResponse(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                sendCommandFailed("register",error);
            }
        });

    }

    private void handleRegisterResponse(JSONObject response){
        try {
            Log.d(LOG,"Response: status: " + response.getString("status") +
                               " reason: " + response.getString("reason"));
            if(response.getString("status").equals("ok"))
                sendRegistrationSucceeded();
            else
                sendRegistrationFailed(response.getString("reason"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getUserInfo(final String username){
        String url = makeURL("get-contact-info",username);
        Log.d(LOG,"getting user info with "+url);

        getJSONCommand(makeURL("get-contact-info",username),
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            if(status.equals("ok")) {
                                sendUserInfo(new UserInfo(response.getString("username"),
                                        response.getString("image"),
                                        response.getString("key")));
                            } else {
                                sendUserNotFound(username);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d(LOG,"Response: " + response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                sendCommandFailed("getUserInfo",error);
            }
        });
    }

    public void login(final String username, final Crypto crypto) {

        if(serverKey!=null) {
            getStringCommand(makeURL("get-challenge", username),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String challenge) {
                            Log.d(LOG,"challenge string "+challenge);
                            if(challenge.equals("user-not-registered")){
                                ServerAPI.userRegistered=false;
                                Log.d(LOG,String.valueOf(userRegistered));
                                sendLoginFailed("user not registered");
                            } else {
                                ServerAPI.userRegistered=true;
                                Log.d(LOG,String.valueOf(userRegistered));
                                processChallengeAndLogin(challenge, username, crypto);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            ServerAPI.userRegistered=false;
                            sendCommandFailed("login", error);
                        }
                    });
        } else {
            ServerAPI.userRegistered=false;
            sendLoginFailed("server key was null");
        }

    }


    private void processChallengeAndLogin(final String challenge, final String username, final Crypto crypto) {
        try {
            Log.v("wasim","server key="+serverKey);
            byte[] decrypted = crypto.decryptRSA(Base64.decode(challenge, Base64.NO_WRAP));
            String response = Base64.encodeToString(Crypto.encryptRSA(decrypted, serverKey), Base64.NO_WRAP);

            putJSONCommand(makeURL("login"), keyValuePairs("username", username,
                    "response", response),
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            handleLoginResponse(response);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            sendCommandFailed("login", error);
                        }
                    });
        } catch (Exception ex) {
            sendLoginFailed(ex.getMessage());
        }

    }

    private void handleLoginResponse(JSONObject response){
        try {
            if(response.getString("status").equals("ok"))
                sendLoginSucceeded();
            else
                sendLoginFailed(response.getString("reason"));
        } catch (JSONException e) {
            e.printStackTrace();
            sendLoginFailed("unable to parse JSON response");
        }
    }


    public void logout(final String username, final Crypto crypto) {
        if(serverKey!=null) {
            getStringCommand(makeURL("get-challenge", username),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String challenge) {
                            if(challenge.equals("user-not-registered")){
                                sendLogoutFailed("user not registered");
                            } else {
                                processChallengeAndLogout(challenge, username, crypto);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            sendCommandFailed("logout", error);
                        }
                    });
        } else {
            sendLogoutFailed("server key was null");
        }
    }


    private void processChallengeAndLogout(final String challenge, final String username, final Crypto crypto) {
        try{
            byte[] decrypted = crypto.decryptRSA(Base64.decode(challenge, Base64.NO_WRAP));
            String response = Base64.encodeToString(Crypto.encryptRSA(decrypted,serverKey), Base64.NO_WRAP);

            putJSONCommand(makeURL("logout"), keyValuePairs("username",username,
                    "response",response),
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            handleLogoutResponse(response);
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            sendCommandFailed("logout",error);
                        }
                    });
        } catch (Exception ex) {
            sendLogoutFailed(ex.getMessage());
        }
    }

    private void handleLogoutResponse(JSONObject response){
        try {
            if(response.getString("status").equals("ok"))
                sendLogoutSucceeded();
            else
                sendLogoutFailed(response.getString("reason"));
        } catch (JSONException e) {
            e.printStackTrace();
            sendLogoutFailed("unable to parse JSON response");
        }
    }

    public void registerContacts(String username, final ArrayList<String> names){
        final JSONObject json = new JSONObject();
        try {
            json.put("username",username);
            json.put("friends",new JSONArray(names));
            putJSONCommand(makeURL("register-friends"), json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            Log.d(LOG, "register friends response " + jsonObject);
                            try {
                                JSONObject status = jsonObject.getJSONObject("friend-status-map");
                                for(String friend : names){

                                    //ArrayList<Contact> con = new ArrayList<>;
                                    String public_key=RandomStringUtils.randomNumeric(6);
                                    Contact contact = new Contact(1,friend,null,public_key);
                                    if(status.getString(friend).equals("logged-in")){
                                        MainActivity.contactStatuses.add(new ContactStatus(friend,true));
                                        contact.setOnline(true);
                                        sendContactLogin(friend);
                                    } else {
                                        MainActivity.contactStatuses.add(new ContactStatus(friend,false));
                                        contact.setOnline(false);
                                        sendContactLogout(friend);
                                    }
                                    boolean b = false;
                                    ArrayList<Contact> con = MyAppication.databaseProvider.getContacts();
                                    for(int i=0;i<con.size();i++) {
                                        if(con.get(i).getUsername().equals(friend))
                                            b=true;
                                    }
                                    if(b==false)
                                    MyAppication.databaseProvider.addContact(contact);
                                    getUserInfo(friend);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Log.d(LOG, "register friends error", volleyError);
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
     * TODO: This currently only supports polling
     */
    public void startPushListener(final String username){
        String url = makeURL("wait-for-push",username);
        Log.d(LOG,"waiting for push with "+url);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        handleNotifications(response);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                sendCommandFailed("pushListener",error);
            }
        });
        jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                60000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        pseudoPushQueue.add(jsObjRequest);
    }

    private void handleNotification(JSONObject notification){
        try {
            String type = notification.getString("type");
            if(type.equals("login")){
                sendContactLogin(notification.getString("username"));
            }
            if(type.equals("logout")){
                sendContactLogout(notification.getString("username"));
            }
            if(type.equals("message")){
                handleMessage(notification.getJSONObject("content"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String decryptAES64ToString(String aes64, SecretKey aesKey) throws UnsupportedEncodingException {
        byte[] bytes = Base64.decode(aes64, Base64.NO_WRAP);
        if(bytes==null) return null;
        bytes = Crypto.decryptAES(bytes, aesKey);
        if(bytes==null) return null;
        return new String(bytes,"UTF-8");
    }

    private void handleMessage(JSONObject message){
        Log.d(LOG,"Got message "+message);
        try{
            SecretKey aesKey = Crypto.getAESSecretKeyFromBytes(myCrypto.decryptRSA(Base64.decode(message.getString("aes-key"), Base64.NO_WRAP)));
            String sender = decryptAES64ToString(message.getString("sender"),aesKey);
            String recipient = decryptAES64ToString(message.getString("recipient"),aesKey);
            String body = decryptAES64ToString(message.getString("body"),aesKey);
            String subject = decryptAES64ToString(message.getString("subject-line"),aesKey);
            Long born = Long.parseLong(decryptAES64ToString(message.getString("born-on-date"),aesKey));
            Long ttl = Long.parseLong(decryptAES64ToString(message.getString("time-to-live"),aesKey));
            Log.d(LOG,sender+" says:");
            Log.d(LOG,subject+":");
            Log.d(LOG,body);
            Log.d(LOG,"ttl: "+ttl);
            sendMessageDelivered(sender,recipient,subject,body,born,ttl);
        } catch (Exception e) {
            Log.d(LOG,"Failed to parse message",e);
        }

    }

    private void handleNotifications(JSONObject notifications){
        try {
            JSONArray array = notifications.getJSONArray("notifications");
            for(int index = 0; index < array.length(); index++){
                handleNotification(array.getJSONObject(index));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String base64AESEncrypted(String clearText, SecretKey aesKey){
        try {
            return Base64.encodeToString(Crypto.encryptAES(clearText.getBytes("UTF-8"),aesKey), Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*
     * The messageReference can be any object. It is used to keep track of which messages
     * succeeded or failed
     */
    public void sendMessage(final Object messageReference,
                            PublicKey recipientKey,
                            String sender,
                            String recipient,
                            String subjectLine,
                            String body,
                            Long bornOnDate,
                            Long timeToLive) {
        SecretKey aesKey = Crypto.createAESKey();
        byte[] aesKeyBytes = aesKey.getEncoded();
        if(aesKeyBytes==null){
            Log.d(LOG,"AES key failed (this should never happen)");
            sendSendMessageFailed(messageReference,"AES key failed");
            return;
        }
        String base64encryptedAESKey =
                Base64.encodeToString(Crypto.encryptRSA(aesKeyBytes,recipientKey),
                        Base64.NO_WRAP);

        putJSONCommand(makeURL("send-message",recipient),
                keyValuePairs("aes-key", base64encryptedAESKey,
                        "sender",  base64AESEncrypted(sender, aesKey),
                        "recipient",  base64AESEncrypted(recipient, aesKey),
                        "subject-line",  base64AESEncrypted(subjectLine, aesKey),
                        "body",  base64AESEncrypted(body, aesKey),
                        "born-on-date",  base64AESEncrypted(bornOnDate.toString(), aesKey),
                        "time-to-live",  base64AESEncrypted(timeToLive.toString(), aesKey)),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        handleSendResponse(messageReference, response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        sendCommandFailed("sendMessage",volleyError);
                        sendSendMessageFailed(messageReference,volleyError.getMessage());
                    }
                });

    }

    private void handleSendResponse(Object reference, JSONObject response){
        try {
            if (response.getString("status").equals("ok")) {
                //Toast.makeText(ourInstance,"message sent",Toast.LENGTH_SHORT).show();
                sendSendMessageSucceeded(reference);
            } else {
                sendSendMessageFailed(reference, response.getString("reason"));
            }
        } catch (JSONException e) {
            sendSendMessageFailed(reference,"possible failure: unable to parse JSON response");
        }
    }

    private void getStringCommand(String url,
                                  Response.Listener<String> listener,
                                  Response.ErrorListener errorListener) {
        commandQueue.add(new StringRequest(Request.Method.GET, url, listener, errorListener));

    }

    private void getJSONCommand(String url,
                                Response.Listener<JSONObject> listener,
                                Response.ErrorListener errorListener){
        commandQueue.add(
                new JsonObjectRequest(Request.Method.GET, url, null, listener,errorListener));

    }

    private void putJSONCommand(String url, JSONObject json,
                                Response.Listener<JSONObject> listener,
                                Response.ErrorListener errorListener){
        commandQueue.add(
                new JsonObjectRequest(Request.Method.PUT, url, json, listener,errorListener));

    }

    private JSONObject keyValuePairs(String... args){
        JSONObject json = new JSONObject();
        try {
            for(int i=0; i+1<args.length;i+=2){
                json.put(args[i],args[i+1]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public class UserInfo{
        public final String username;
        public final String image;
        public final PublicKey publicKey;
        public UserInfo(String username, String image, String keyString){
            this.username = username;
            this.image = image;
            this.publicKey = Crypto.getPublicKeyFromString(keyString);
        }
    }

    public interface Listener {
        void onCommandFailed(String commandName, VolleyError volleyError);
        void onGoodAPIVersion();
        void onBadAPIVersion();
        void onRegistrationSucceeded();
        void onRegistrationFailed(String reason);
        void onLoginSucceeded();
        void onLoginFailed(String reason);
        void onLogoutSucceeded();
        void onLogoutFailed(String reason);
        void onUserInfo(UserInfo info);
        void onUserNotFound(String username);
        void onContactLogin(String username);
        void onContactLogout(String username);
        void onSendMessageSucceeded(Object key);
        void onSendMessageFailed(Object key, String reason);
        void onMessageDelivered(String sender, String recipient,
                                String subject, String body,
                                long born_on_date,
                                long time_to_live);

    }

    private ArrayList<Listener> myListeners = new ArrayList<>();

    public void registerListener(Listener listener){myListeners.add(listener);}
    public void unregisterListener(Listener listener){myListeners.remove(listener);}

    private void sendCommandFailed(String commandName, VolleyError volleyError){
        for(Listener listener : myListeners){
            listener.onCommandFailed(commandName,volleyError);
        }
    }

    private void sendGoodAPIVersion(){

        for(Listener listener : myListeners){
            listener.onGoodAPIVersion();
        }

    }

    private void sendBadAPIVersion(){
        for(Listener listener : myListeners){
            listener.onBadAPIVersion();
        }
    }

    private void sendRegistrationSucceeded(){
        for(Listener listener : myListeners){
            listener.onRegistrationSucceeded();
        }
    }
    private void sendRegistrationFailed(String reason){
        for(Listener listener : myListeners){
            listener.onRegistrationFailed(reason);
        }
    }

    private void sendLoginSucceeded(){
        for(Listener listener : myListeners){
            listener.onLoginSucceeded();
        }
    }

    private void sendLoginFailed(String reason){
        for(Listener listener : myListeners){
            listener.onLoginFailed(reason);
        }
    }

    private void sendLogoutSucceeded(){
        for(Listener listener : myListeners){
            listener.onLogoutSucceeded();
        }
    }

    private void sendLogoutFailed(String reason){
        for(Listener listener : myListeners){
            listener.onLogoutFailed(reason);
        }
    }
    private void sendUserInfo(UserInfo info){
        for(Listener listener : myListeners){
            listener.onUserInfo(info);
        }
    }
    private void sendUserNotFound(String username){
        for(Listener listener : myListeners){
            listener.onUserNotFound(username);
        }
    }
    private void sendContactLogin(String username){
        for(Listener listener : myListeners){
            listener.onContactLogin(username);
        }
    }
    private void sendContactLogout(String username){
        for(Listener listener : myListeners){
            listener.onContactLogout(username);
        }
    }

    private void sendSendMessageSucceeded(Object key){
        for(Listener listener: myListeners){
            listener.onSendMessageSucceeded(key);
        }
    }

    private void sendSendMessageFailed(Object key, String reason){
        for(Listener listener: myListeners){
            listener.onSendMessageFailed(key, reason);
        }
    }

    private void sendMessageDelivered(String sender, String recipient,
                                      String subject, String body,
                                      long born_on_date,
                                      long time_to_live){
        for(Listener listener : myListeners) {
            listener.onMessageDelivered(
                    sender,recipient,
                    subject,body,
                    born_on_date,time_to_live);
        }
    }

}