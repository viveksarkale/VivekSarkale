package chat.application.model;

/**
 * Created by Pbuhsoft on 06/07/2016.
 */
public class Message {
    private int id;
    private String username;
    private String subject;
    private String msg_body;
    private  long time_to_live;
    public Message(int id,String username,String subject
            ,String msg_body,long time_to_live){
        this.id=id;
        this.username=username;
        this.subject=subject;
        this.msg_body=msg_body;
        this.time_to_live=time_to_live;
    }

    public int getId() {
        return id;
    }

    public long getTime_to_live() {
        return time_to_live;
    }

    public String getMsg_body() {
        return msg_body;
    }

    public String getSubject() {
        return subject;
    }

    public String getUsername() {
        return username;
    }

}
