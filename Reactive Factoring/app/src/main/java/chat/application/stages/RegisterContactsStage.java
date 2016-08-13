package chat.application.stages;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import chat.application.MainActivity;
import chat.application.activity.ContactActivity;
import chat.application.model.Contact;
import chat.application.server.Notification;
import chat.application.server.WebHelper;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by kbaldor on 7/28/16.
 */
public class RegisterContactsStage implements Func1<String, Observable<Notification>> {

    private static final String LOG ="RegisterContactsStage";
    final String server;
    final String username;
    final List<Contact> contacts;


    public RegisterContactsStage(String server, String username, List<Contact> contacts){
        this.server = server;
        this.username = username;
        this.contacts = contacts;
    }

    @Override
    public Observable<Notification> call(String challenge_response)  {
        try {
            JSONObject json = new JSONObject();
            json.put("username",username);
            json.put("friends",new JSONArray(contacts));
            JSONObject response = WebHelper.JSONPut(server+"/register-friends",json);

            ArrayList<Notification> notifications = new ArrayList<>();
            JSONObject status = response.getJSONObject("friend-status-map");
            int loc=0;
            for(Contact contact : contacts){
                Log.d(LOG,"username="+contact.getUsername());
                if(status.getString(contact.getUsername()).equals("logged-in")){
                    notifications.add(new Notification.LogIn(contact.getUsername()));
                    contacts.get(loc).setOnline(true);
                } else {
                    notifications.add(new Notification.LogOut(contact.getUsername()));
                    contacts.get(loc).setOnline(false);
                }
                loc++;
            }

            return Observable.from(notifications);
        } catch (Exception e) {
            e.printStackTrace();
            return Observable.error(e);
        }
    }
}

