package chat.application.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import chat.application.R;
import chat.application.database.DatabaseProvider;

@SuppressWarnings("ALL")
public class ReadMessageActivity extends AppCompatActivity
{
    private int msg_id;
    private String senderName, subject, body;
    private TextView tvSenderName, tvSubject, tvBody, tvReply;
    private ImageView imDelete;
    private DatabaseProvider databaseProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_message);
        databaseProvider=new DatabaseProvider(ReadMessageActivity.this);
        Intent i = getIntent();
        if (i != null)
        {
            msg_id =i.getIntExtra("id",0);
            senderName = i.getStringExtra("senderName");
            subject = i.getStringExtra("subject");
            body = i.getStringExtra("body");
        }

        init();
        setClickEvent();
    }

    private void init()
    {
        tvSenderName = (TextView) findViewById(R.id.tv_ReadMessage_senderName);
        tvSubject = (TextView) findViewById(R.id.tv_ReadMessage_subject);
        tvBody = (TextView) findViewById(R.id.tv_ReadMessage_body);
        tvReply = (TextView) findViewById(R.id.tv_readMessage_reply);

        imDelete = (ImageView) findViewById(R.id.image_read_delete);

        tvSenderName.setText("" + senderName);
        tvSubject.setText("" + subject);
        tvBody.setText("" + body);

    }

    private void setClickEvent()
    {
        imDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseProvider.deleteMessage(msg_id);
                ReadMessageActivity.this.finish();
            }
        });

        tvReply.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ComposeActivity.checkIntent = "Check";
                Intent i = new Intent(ReadMessageActivity.this, ComposeActivity.class);
                i.putExtra("senderName", senderName);
                i.putExtra("subject", subject);
                startActivity(i);
            }
        });
    }
}
