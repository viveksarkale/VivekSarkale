package chat.application;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Random;

import chat.application.activity.LoginActivity;
import chat.application.database.DatabaseProvider;
import chat.application.helper.MySharedPreferences;
import chat.application.model.Contact;
import chat.application.model.Message;
import chat.application.server.ServerAPI;

/**
 * Created by Pbuhsoft on 06/07/2016.
 */
public class MyAppication extends Application{
    public static final String KEY_PAIR="key_pair";
    public static DatabaseProvider databaseProvider;
    public static final long ONE_SECOND=1000;
    //public static ServerAPI serverAPI;

    @Override
    public void onCreate() {
        super.onCreate();
        databaseProvider=new DatabaseProvider(getApplicationContext());
        String keyPair="IAmAdmin:"+ "myfavkeypair";
        //set keypair in preference...
        MySharedPreferences.saveToPreference(getApplicationContext(),KEY_PAIR,keyPair);

        /*
        //genetating 3 fake contacts....
        Contact contact1=new Contact(1,"user101",null, RandomStringUtils.randomAlphabetic(5));
        Contact contact2=new Contact(2,"user102",null,RandomStringUtils.randomAlphabetic(5));
        Contact contact3=new Contact(3,"user103",null,RandomStringUtils.randomAlphabetic(5));
        databaseProvider.addContact(contact1);
        databaseProvider.addContact(contact2);
        databaseProvider.addContact(contact3);*/




    }

}
