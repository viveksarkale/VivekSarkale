package chat.application.server;

/**
 * Created by Divyanshu on 7/28/2016.
 */
public class ContactStatus {
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    String user;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    boolean status=false;

    ContactStatus(String user, boolean status) {
        this.user = user;
        this.status = status;
    }
}
