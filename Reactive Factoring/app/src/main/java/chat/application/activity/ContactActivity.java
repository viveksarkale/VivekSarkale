package chat.application.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import chat.application.MainActivity;
import chat.application.MyAppication;
import chat.application.R;
import chat.application.adapter.ContactAdapter;
import chat.application.database.DatabaseProvider;
import chat.application.helper.NonScrollListView;
import chat.application.model.Contact;
import chat.application.model.ContactObject;
import chat.application.server.Crypto;

import chat.application.server.Notification;
import chat.application.stages.GetChallengeStage;
import chat.application.stages.GetServerKeyStage;
import chat.application.stages.LogInStage;
import chat.application.stages.RegisterContactsStage;
import chat.application.stages.RegistrationStage;
import nz.sodium.CellLoop;
import nz.sodium.Handler;
import nz.sodium.Lambda2;
import nz.sodium.Stream;
import nz.sodium.StreamSink;
import nz.sodium.Transaction;
import nz.sodium.Unit;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.schedulers.TimeInterval;

@SuppressWarnings("ALL")
public class ContactActivity extends AppCompatActivity
{
    private static final String LOG = "ContactActivity";
    private NonScrollListView listContact;
    private ArrayList<Contact> list = new ArrayList<>();
    private ContactAdapter adapter;
    private DatabaseProvider databaseProvider;
    private Crypto myCrypto;
    public static final String server_name = "http://129.115.27.54:25666";

    StreamSink<Notification.LogIn> loginEvent = new StreamSink<>();
    StreamSink<Notification.LogOut> logoutEvent = new StreamSink<>();
    StreamSink<Contact> addContactEvent = new StreamSink<>();
    StreamSink<Contact> removeContactEvent = new StreamSink<>();
    public static ContactActivity contactActivity;
    CellLoop<ArrayList<Contact>> cellLoopContacts;
    //    private ImageView imAddContact;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        contactActivity=this;
        databaseProvider = MyAppication.databaseProvider;
        ArrayList<String> contacts = new ArrayList<>();
        contacts.add("alice");
        contacts.add("bob");
        init();


    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void init()
    {
        //databaseProvider=new DatabaseProvider(ContactActivity.this);
        listContact = (NonScrollListView) findViewById(R.id.list_contact_view);
        myCrypto = new Crypto(getPreferences(Context.MODE_PRIVATE));
        list= MainActivity.userList;
        adapter = new ContactAdapter(ContactActivity.this, R.layout.item_contact, list);
        listContact.setAdapter(adapter);
        Log.d("adapter set","true");
        listContact.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d("view","clicked");
                ComposeActivity.checkIntent = "Check";
                Intent i = new Intent(ContactActivity.this, ComposeActivity.class);
                i.putExtra("senderName", list.get(position).getUsername());
                i.putExtra("subject", "");
                startActivity(i);
            }
        });
        refreshList();
        //calling this function for apply reactive network...
        myReactiveNetwork();

    }
public void myReactiveNetwork(){

    Observable.just(0) // the value doesn't matter, it just kicks things off
            .observeOn(Schedulers.newThread())
            .subscribeOn(Schedulers.newThread())
            .flatMap(new GetServerKeyStage(server_name))
            .flatMap(new RegistrationStage(server_name, LoginActivity.uName,
                    getBase64Image(), myCrypto.getPublicKeyString()))
            .flatMap(new GetChallengeStage(server_name,LoginActivity.uName,myCrypto))
            .flatMap(new LogInStage(server_name, LoginActivity.uName))
            .flatMap(new RegisterContactsStage(server_name, LoginActivity.uName, list))
            .subscribe(new Observer<Notification>() {
                @Override
                public void onCompleted() {

                    // now that we have the initial state, start polling for updates

                    Observable.interval(0,1, TimeUnit.SECONDS, Schedulers.newThread())
                            .subscribeOn(AndroidSchedulers.mainThread())
                            //   .take(5) // would only poll five times
                            //   .takeWhile( <predicate> ) // could stop based on a flag variable
                            .subscribe(new Observer<Long>() {
                                @Override
                                public void onCompleted() {
                                    Log.d("LOG","inner onCompleted()");
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.d("LOG","Error: ",e);
                                }

                                @Override
                                public void onNext(Long numTicks) {
                                    Log.d("POLL","Polling "+numTicks);

                                }
                            });
                }

                @Override
                public void onError(Throwable e) {
                    Log.d("LOG","Error: ",e);
                }

                @Override
                public void onNext(Notification notification) {
                    // handle initial state here
                    adapter.notifyDataSetChanged();
                    Log.d("LOG","Next "+ notification);
                    if(notification instanceof Notification.LogIn) {
                        Log.v("LOG","User "+((Notification.LogIn)notification).username+" is logged in");
                        loginEvent.send((Notification.LogIn)notification);
                    }
                    if(notification instanceof Notification.LogOut) {
                        Log.v("LOG","User "+((Notification.LogOut)notification).username+" is logged out");
                        logoutEvent.send((Notification.LogOut)notification);
                    }
                }
            });

        Transaction.runVoid(new Runnable() {
            @Override
            public void run() {
                cellLoopContacts=new CellLoop<ArrayList<Contact>>();
                Stream<ArrayList<Contact>> addContactStream = addContactEvent.snapshot(cellLoopContacts, new Lambda2<Contact, ArrayList<Contact>, ArrayList<Contact>>() {
                    @Override
                    public ArrayList<Contact> apply(Contact contact, ArrayList<Contact> contacts) {
                        contacts.add(contact);
                        return contacts;
                    }
                });
                Stream<ArrayList<Contact>> removeContactStream = removeContactEvent.snapshot(cellLoopContacts, new Lambda2<Contact, ArrayList<Contact>, ArrayList<Contact>>() {
                    @Override
                    public ArrayList<Contact> apply(Contact contact, ArrayList<Contact> contacts) {
                        contacts.remove(contact);
                        return contacts;
                    }
                });
                Stream<ArrayList<Contact>> loginUserStream = loginEvent.snapshot(cellLoopContacts, new Lambda2<Notification.LogIn, ArrayList<Contact>, ArrayList<Contact>>() {
                    @Override
                    public ArrayList<Contact> apply(Notification.LogIn logIn, ArrayList<Contact> contacts) {
                        ArrayList<Contact> updatedContacts = new ArrayList<Contact>();
                        for (Contact user : contacts) {
                            if (logIn.username.equals(user.getUsername())) {
                                user.setOnline(true);
                            }
                            updatedContacts.add(user);
                        }
                        return updatedContacts;
                    }
                });
                Stream<ArrayList<Contact>> logoutUserStream = logoutEvent.snapshot(cellLoopContacts, new Lambda2<Notification.LogOut, ArrayList<Contact>, ArrayList<Contact>>() {
                    @Override
                    public ArrayList<Contact> apply(Notification.LogOut logOut, ArrayList<Contact> contacts) {
                        ArrayList<Contact> updatedContacts = new ArrayList<Contact>();
                        for (Contact user : contacts) {
                            if (logOut.username.equals(user.getUsername())) {
                                user.setOnline(false);
                            }
                            updatedContacts.add(user);
                        }
                        return updatedContacts;
                    }
                });

                Stream<ArrayList<Contact>> contactsGroup = (addContactStream.orElse(removeContactStream)).orElse(logoutUserStream);

                Stream<ArrayList<Contact>> mergedContacts = loginUserStream.merge(contactsGroup, new Lambda2<ArrayList<Contact>, ArrayList<Contact>, ArrayList<Contact>>() {
                    @Override
                    public ArrayList<Contact> apply(ArrayList<Contact> contacts, ArrayList<Contact> contacts2) {
                        return contacts;
                    }
                });
                cellLoopContacts.loop(mergedContacts.hold(list));
            }
        });

    cellLoopContacts.listen(new Handler<ArrayList<Contact>>() {
        @Override
        public void run(ArrayList<Contact> contacts) {
            //apply changes to listview to see updated status of users...
            //adapter = new ContactAdapter(ContactActivity.this, R.layout.item_contact,MainActivity.userList);
            //listContact.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            Log.v("LOG","updating listview");
        }
    });

}
    public void notifyRemovedContact(Contact contact){
        addContactEvent.send(contact);
    }
    public void notifyAddedContact(Contact contact){
        removeContactEvent.send(contact);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    String getBase64Image(){
        InputStream is;
        byte[] buffer = new byte[0];
        try {
            is = getAssets().open("images/ic_android_black_24dp.png");
            buffer = new byte[is.available()];
            is.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(buffer,Base64.DEFAULT).trim();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent i = new Intent(ContactActivity.this, AddContactActivity.class);
                AddContactActivity.ISNEWCONTACT=true;
                startActivityForResult(i,101);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v("ContactActivity","ContactActivity onRestart()");
        refreshList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==101){
            refreshList();
        }
    }
    public void refreshList(){
        list=databaseProvider.getContacts();
        adapter = new ContactAdapter(ContactActivity.this, R.layout.item_contact, list);
        listContact.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    //public void

    @Override
    public void onBackPressed() {

        Bundle bundle = getIntent().getExtras();
        //Log.d("PREV ACT",ContactActivity.this.getCallingActivity().getClassName());
        if(bundle.getBoolean("fromCompose")) {
            ContactActivity.this.finish();
        }
        super.onBackPressed();
    }
}
