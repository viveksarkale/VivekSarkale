package chat.application.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import chat.application.MainActivity;
import chat.application.MyAppication;
import chat.application.R;

@SuppressWarnings("ALL")
public class ComposeActivity extends AppCompatActivity
{
    private TextView tvSend, tvTo;
    private EditText /*edTo,*/ edSubject, edBody;
    private String sTo = "", sSubject = "";
    public static String checkIntent = "";
    private ImageView deleteMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        init();
        setClickEvent();

        if (!checkIntent.equals(""))
        {
            Intent i = getIntent();
            if (i != null)
            {
                sTo = i.getStringExtra("senderName");
                sSubject = i.getStringExtra("subject");

                tvTo.setText("" + sTo);
                edSubject.setText("" + sSubject);
                checkIntent = "";
            }
        }
    }

    private void init()
    {
        tvSend = (TextView) findViewById(R.id.tv_Compose_send);
        tvTo = (TextView) findViewById(R.id.tv_Compose_To);
        edSubject = (EditText) findViewById(R.id.ed_Compose_subject);
        edBody = (EditText) findViewById(R.id.ed_Compose_body);
        deleteMessage = (ImageView) findViewById(R.id.delete_current_message);
    }

    public void doSendMessageToAnyone(String sender,String recipient,String subject,String body){
        LoginActivity.serverAPI.setServerName(LoginActivity.serverName);

        if(LoginActivity.myUserMap.containsKey(recipient)) {
            LoginActivity.serverAPI.sendMessage(new Object(), // I don't have an object to keep track of, but I need one!
                    LoginActivity.myUserMap.get(recipient).publicKey,
                    LoginActivity.uName,
                    recipient,
                    subject,
                    body,
                    System.currentTimeMillis(),
                    (long) 1500);
        } else {
            Log.d("Main",recipient+" info not available");
        }
    }

    private void setClickEvent()
    {
        tvTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ComposeActivity.this, ContactActivity.class);
                i.putExtra("fromCompose",true);
                startActivity(i);
            }
        });

        tvSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Toast.makeText(ComposeActivity.this,edBody.getText(),Toast.LENGTH_LONG).show();
                doSendMessageToAnyone(LoginActivity.uName,tvTo.getText().toString(),edSubject.getText().toString(),edBody.getText().toString());
                ComposeActivity.this.finish();
            }
        });

        deleteMessage.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            ComposeActivity.this.finish();
        }
        });

    }
}
