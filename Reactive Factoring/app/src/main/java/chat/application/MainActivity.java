package chat.application;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import chat.application.activity.ComposeActivity;
import chat.application.activity.ContactActivity;
import chat.application.activity.LoginActivity;
import chat.application.activity.ReadMessageActivity;
import chat.application.activity.SettingActivity;
import chat.application.adapter.MainAdapter;
import chat.application.database.DatabaseProvider;
import chat.application.helper.MySharedPreferences;
import chat.application.helper.NonScrollListView;
import chat.application.model.Contact;
import chat.application.model.Message;
import chat.application.server.ContactStatus;
import chat.application.server.Crypto;
import chat.application.server.ServerAPI;

public class MainActivity extends AppCompatActivity
{
    public static final String KEY_PAIR="key_pair";
    private LinearLayout lvSetting, lvContact, lvCompose;
    private NonScrollListView listMain;
    private MainAdapter adapter;
    private DatabaseProvider databaseProvider;
    private ArrayList<Message> list = new ArrayList<>();
    public static ArrayList<Contact> userList = new ArrayList<>();
    public static ArrayList<ContactStatus> contactStatuses = new ArrayList<>();
    public static final int REQ_MESSAGE=101;
    public static final int REQ_SETTING=102;
    public static final int REQ_CONTACT=103;
    public static final int REQ_COMPOSE=104;
    public static final String ISFIRSTUSE="isfirstuse";
    Handler handler1,handler2,handler3;
    public static MainActivity mMainActivity;
    public static String loginStatus[] = new String[2];

    private String LOG = "MainActivity";

    //public static HashMap<String,ServerAPI.UserInfo> myUserMap;
    ServerAPI inServerAPI;
    Crypto inCrypto;
    //String serverName;


    public void getLogin() {

    }


    private String getUserName(){
        return LoginActivity.uName;
    }

    public String getServerName(){
        //SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        return LoginActivity.serverName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //myUserMap = LoginActivity.
        mMainActivity=MainActivity.this;
        inServerAPI = LoginActivity.serverAPI;
        inCrypto = LoginActivity.myCrypto;

        databaseProvider=new DatabaseProvider(getApplicationContext());
        userList = MyAppication.databaseProvider.getContacts();
        Log.v(LOG,"size of list="+userList.size());
        LoginActivity.serverAPI.setServerName(getServerName());
        LoginActivity.serverAPI.setServerPort("25666");

        Log.d(LOG,"mycrpyto : "+LoginActivity.myCrypto.getPublicKeyString());

        String keyPair="IAmAdmin:"+ LoginActivity.uName;
        //set keypair in preference...
        MySharedPreferences.saveToPreference(getApplicationContext(),KEY_PAIR,keyPair);

        doRegisterContacts("alice");
        doRegisterContacts("bob");


        doStartPushListener();


        //generating 3 fake messages..
        Message message1=new Message(1,"user101","here is subject1"
                ,"This is message1.",5*MyAppication.ONE_SECOND);
        Message message2=new Message(2,"user102","here is subject2"
                ,"This is message2.",10*MyAppication.ONE_SECOND);
        Message message3=new Message(3,"user103","here is subject3"
                ,"This is message3.",15*MyAppication.ONE_SECOND);
        databaseProvider.addNewMessage(message1);
        databaseProvider.addNewMessage(message2);
        databaseProvider.addNewMessage(message3);

        init();
        setClickEvent();

        /*LoginActivity.serverAPI.registerListener(new ServerAPI.Listener() {
            @Override
            public void onCommandFailed(String commandName, VolleyError volleyError) {
                //Toast.makeText(LoginActivity.this, String.format("command %s failed!",commandName),Toast.LENGTH_SHORT).show();
                //volleyError.printStackTrace();
            }

            @Override
            public void onGoodAPIVersion() {
                //Toast.makeText(LoginActivity.this,"API Version Matched!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onBadAPIVersion() {
                //Toast.makeText(LoginActivity.this,"API Version Mismatch!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRegistrationSucceeded() {
                //Toast.makeText(LoginActivity.this,"Registered!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRegistrationFailed(String reason) {
                //Toast.makeText(LoginActivity.this,"Not registered!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLoginSucceeded() {
                *//*getPreferences(Context.MODE_PRIVATE).edit().putString("ServerName",((EditText) findViewById(R.id.servername)).getText().toString()).commit();
                getPreferences(Context.MODE_PRIVATE).edit().putString("UserName",((EditText) findViewById(R.id.username)).getText().toString()).commit();
                Toast.makeText(LoginActivity.this,"Logged in!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(i);*//*
            }

            @Override
            public void onLoginFailed(String reason) {
                //Toast.makeText(LoginActivity.this,"Not logged in : "+reason, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLogoutSucceeded() {
                //MainActivity.contactStatuses.clear();
                //Toast.makeText(LoginActivity.this,"Logged out!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLogoutFailed(String reason) {
                //Toast.makeText(LoginActivity.this,"Not logged out!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUserInfo(ServerAPI.UserInfo info) {
                //myUserMap.put(info.username,info);
            }

            @Override
            public void onUserNotFound(String username) {
                Toast.makeText(MainActivity.this, String.format("user %s not found!",username), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onContactLogin(String username) {

                Toast.makeText(MainActivity.this, String.format("user %s logged in",username), Toast.LENGTH_SHORT).show();
                //new ContactActivity().refreshList();
            }

            @Override
            public void onContactLogout(String username) {
                Toast.makeText(MainActivity.this, String.format("user %s logged out",username), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSendMessageSucceeded(Object key) {
                Toast.makeText(MainActivity.this, String.format("sent a message"), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSendMessageFailed(Object key, String reason) {
                Toast.makeText(MainActivity.this, String.format("failed to send a message"), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMessageDelivered(String sender, String recipient, String subject, String body, long born_on_date, long time_to_live) {
                Toast.makeText(MainActivity.this, String.format("got message from %s",sender), Toast.LENGTH_SHORT).show();
                Message message = new Message(1,sender,subject,body,time_to_live);
                databaseProvider.addNewMessage(message);
            }
        });*/
    }

    private void init()
    {
        lvSetting = (LinearLayout) findViewById(R.id.linear_setting);
        lvContact = (LinearLayout) findViewById(R.id.linear_contact);
        lvCompose = (LinearLayout) findViewById(R.id.linear_Compose);

        listMain = (NonScrollListView) findViewById(R.id.list_main);
        databaseProvider=new DatabaseProvider(MainActivity.this);
        list=databaseProvider.getMessages();

        adapter = new MainAdapter(MainActivity.this, R.layout.item_main, list);
        listMain.setAdapter(adapter);

        boolean is_first_use= MySharedPreferences.readBooleanFromPreference(MainActivity.this
                ,ISFIRSTUSE,false);
        if (!is_first_use) {
            MySharedPreferences.saveBooleanToPreference(MainActivity.this, ISFIRSTUSE, true);
        }


            //scheduling deletion of messages according to their TIME_TO_LIVE..

            MyRunnable myRunnable1=new MyRunnable(list.get(0).getId());
            MyRunnable myRunnable2=new MyRunnable(list.get(1).getId());
            MyRunnable myRunnable3=new MyRunnable(list.get(2).getId());
            //setting handler for each task...

            handler1=new Handler();
            handler2=new Handler();
            handler3=new Handler();

            handler1.postDelayed(myRunnable1,list.get(0).getTime_to_live());
            handler2.postDelayed(myRunnable2,list.get(1).getTime_to_live());
            handler3.postDelayed(myRunnable3,list.get(2).getTime_to_live());


        /*ExecutorService threadpool = Executors.newFixedThreadPool(10);
        for(int i=0;i<databaseProvider.getMessages().size();i++) {
            threadpool.submit(new MyRunnable(list.get(i).getId()));

        }


        try {
            threadpool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //threadpool.shutdown();*/

    }

    private void setClickEvent()
    {
        listMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(MainActivity.this, ReadMessageActivity.class);
                i.putExtra("id", list.get(position).getId());
                i.putExtra("senderName", list.get(position).getUsername());
                i.putExtra("subject", list.get(position).getSubject());
                i.putExtra("body", list.get(position).getMsg_body());
                startActivityForResult(i,REQ_MESSAGE);
            }
        });

        lvSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(i);
            }
        });

        lvContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ContactActivity.class);
                i.putExtra("fromCompose",false);
                startActivity(i);
            }
        });

        lvCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ComposeActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQ_MESSAGE){
            list=databaseProvider.getMessages();
            adapter = new MainAdapter(MainActivity.this, R.layout.item_main, list);
            listMain.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

    }
    class MyRunnable implements Runnable
    {
        private int id;
        private DatabaseProvider databaseProvider;
        public MyRunnable(int id){
            this.id=id;
            databaseProvider=new DatabaseProvider(getApplicationContext());

        }
        @Override
        public void run() {
            Log.v("MyRunnable","going delete msg by handler..");
            databaseProvider.deleteMessage(id);
            list=databaseProvider.getMessages();
            adapter = new MainAdapter(MainActivity.this, R.layout.item_main, list);
            listMain.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    public void doLogout() {
        inServerAPI.setServerName(getServerName());
        inServerAPI.logout(getUserName(),inCrypto);
    }

    public  void doRegisterContacts(String user){
        inServerAPI.setServerName(getServerName());
        //Log.d("DB",userList.toArray());
        boolean flag = false;
        Log.d("UserList size ",""+userList.size());
        /*for(int i=0;i<userList.size();i++) {
            Log.d("Users",userList.get(i).getUsername());
            if(user.equals(userList.get(i).getUsername())) {
                flag = true;
            }
        }*/

        ArrayList<String> contacts = new ArrayList<>();
        if(flag==false) {

        contacts.add(user);}
        //databaseProvider.addContact();
        inServerAPI.registerContacts(getUserName(),contacts);
    }

    public void doStartPushListener() {
        inServerAPI.setServerName(getServerName());

        inServerAPI.startPushListener(LoginActivity.uName);

    }

    public void doGetAliceInfo(View view){
        inServerAPI.setServerName(getServerName());
        inServerAPI.getUserInfo("alice");
    }

    public void doGetNobodyInfo(View view){
        inServerAPI.setServerName(getServerName());
        inServerAPI.getUserInfo("a_name_that_doesnt_exist");
    }



    public void doSendMessageToAlice(View view){
        inServerAPI.setServerName(getServerName());

        if(LoginActivity.myUserMap.containsKey("alice")) {
            inServerAPI.sendMessage(new Object(), // I don't have an object to keep track of, but I need one!
                    LoginActivity.myUserMap.get("alice").publicKey,
                    getUserName(),
                    "alice",
                    "test message",
                    "test body",
                    System.currentTimeMillis(),
                    (long) 15000);
        } else {
            Log.d("Main","Alice info not available");
        }
    }
    @Override
    public void onBackPressed()
    {
        doLogout();
        super.onBackPressed();
    }
}
