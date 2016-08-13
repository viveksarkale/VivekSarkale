package chat.application.model;

import android.graphics.Bitmap;

/**
 * Created by Pbuhsoft on 06/07/2016.
 */
public class Contact {
    private int id;
    private String username;
    private byte[] image;
    private String public_key;
    private boolean online;

    public void setOnline(boolean on){
        this.online = on;
    }
    public Contact(int id,String username,byte[] image,String public_key){
        this.id=id;
        this.username=username;
        this.image=image;
        this.public_key=public_key;
        this.online = false;
    }

    public String getUsername() {
        return username;
    }

    public int getId() {
        return id;
    }

    public byte[] getImage() {
        return image;
    }

    public String getPublic_key() {
        return public_key;
    }

    public boolean getOnline() {
        return online;
    }

}
