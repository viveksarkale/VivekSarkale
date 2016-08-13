package chat.application.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import chat.application.MainActivity;
import chat.application.MyAppication;
import chat.application.R;
import chat.application.server.Crypto;
import chat.application.server.ServerAPI;

public class LoginActivity extends AppCompatActivity {



    boolean chAPI;
    public static Crypto myCrypto;
    public static String serverName;
    public static String uName;
    public static ServerAPI serverAPI;

    public static HashMap<String,ServerAPI.UserInfo> myUserMap = new HashMap<>();

    //public


    public String getUserName(){
        uName = ((EditText)findViewById(R.id.username)).getText().toString();
        return uName;
    }

    private String getServerName(){
        serverName = ((EditText)findViewById(R.id.servername)).getText().toString();
        return serverName;
        //return "129.115.27.54";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        serverName = getPreferences(Context.MODE_PRIVATE).getString("ServerName","129.115.27.54");

        ((EditText)findViewById(R.id.servername)).setText(serverName);

        myCrypto = new Crypto(getPreferences(Context.MODE_PRIVATE));
        myCrypto.saveKeys(getPreferences(Context.MODE_PRIVATE));

        serverAPI = ServerAPI.getInstance(this.getApplicationContext(),myCrypto);

        //myUserMap = new HashMap<>();

        //MyAppication.serverAPI = serverAPI;

        //serverAPI.checkAPIVersion();

        //serverAPI = MyAppication.serverAPI;

        serverAPI.setServerName(getServerName());
        serverAPI.setServerPort("25666");

        serverAPI.registerListener(new ServerAPI.Listener() {
            @Override
            public void onCommandFailed(String commandName, VolleyError volleyError) {
                Toast.makeText(LoginActivity.this, String.format("command %s failed!",commandName),
                        Toast.LENGTH_SHORT).show();
                volleyError.printStackTrace();
            }

            @Override
            public void onGoodAPIVersion() {
                chAPI=true;
                Toast.makeText(LoginActivity.this,"API Version Matched!", Toast.LENGTH_SHORT).show();
                doLogin();
            }

            @Override
            public void onBadAPIVersion() {
                Toast.makeText(LoginActivity.this,"API Version Mismatch!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRegistrationSucceeded() {
                Toast.makeText(LoginActivity.this,"Registered!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRegistrationFailed(String reason) {
                Toast.makeText(LoginActivity.this,"Not registered!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoginSucceeded() {
                getPreferences(Context.MODE_PRIVATE).edit().putString("ServerName",((EditText) findViewById(R.id.servername)).getText().toString()).commit();
                getPreferences(Context.MODE_PRIVATE).edit().putString("UserName",((EditText) findViewById(R.id.username)).getText().toString()).commit();
                Toast.makeText(LoginActivity.this,"Logged in!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(i);
            }

            @Override
            public void onLoginFailed(String reason) {
                Toast.makeText(LoginActivity.this,"Not logged in : "+reason, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLogoutSucceeded() {
                //MainActivity.contactStatuses.clear();
                Toast.makeText(LoginActivity.this,"Logged out!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLogoutFailed(String reason) {
                Toast.makeText(LoginActivity.this,"Not logged out!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUserInfo(ServerAPI.UserInfo info) {
                myUserMap.put(info.username,info);
            }

            @Override
            public void onUserNotFound(String username) {
                Toast.makeText(LoginActivity.this, String.format("user %s not found!",username), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onContactLogin(String username) {

                Toast.makeText(LoginActivity.this, String.format("user %s logged in",username), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onContactLogout(String username) {
                Toast.makeText(LoginActivity.this, String.format("user %s logged out",username), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSendMessageSucceeded(Object key) {
                Toast.makeText(LoginActivity.this, String.format("sent a message"), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSendMessageFailed(Object key, String reason) {
                Toast.makeText(LoginActivity.this, String.format("failed to send a message"), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMessageDelivered(String sender, String recipient, String subject, String body, long born_on_date, long time_to_live) {
                Toast.makeText(LoginActivity.this, String.format("got message from %s",sender), Toast.LENGTH_SHORT).show();

            }
        });

    }

    /*protected void onPause() {
        super.onPause();
        String serverName = ((EditText)findViewById(R.id.servername)).getText().toString();
        getPreferences(Context.MODE_PRIVATE).edit().putString("ServerName",serverName).commit();
    }*/

    public void doCheckAPIVersion(View view){
        serverAPI.setServerName(getServerName());
        getUserName();
        if(uName != null && !uName.isEmpty())
            serverAPI.checkAPIVersion();
        else
            Toast.makeText(LoginActivity.this,"Please enter a username",Toast.LENGTH_SHORT).show();
    }

    public void doRegister(View view) {

        String username = ((EditText)findViewById(R.id.username)).getText().toString();

        if(username.isEmpty()) {
            Toast.makeText(LoginActivity.this,"Please enter a username",Toast.LENGTH_SHORT).show();
        }
        else {
            serverAPI.setServerName(getServerName());

            InputStream is;
            byte[] buffer = new byte[0];
            try {
                is = getAssets().open("images/ic_android_black_24dp.png");
                buffer = new byte[is.available()];
                is.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            serverAPI.register(username, Base64.encodeToString(buffer, Base64.DEFAULT).trim(), myCrypto.getPublicKeyString());
        }}

    public void doLogin() {

        serverAPI.setServerName(getServerName());
        serverAPI.login(getUserName(), myCrypto);

    }
}
