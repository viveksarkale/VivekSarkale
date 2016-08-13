package chat.application.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.RandomStringUtils;

import chat.application.MainActivity;
import chat.application.R;
import chat.application.adapter.ContactAdapter;
import chat.application.database.DatabaseProvider;
import chat.application.model.Contact;

@SuppressWarnings("ALL")
public class AddContactActivity extends AppCompatActivity
{
    public static boolean ISNEWCONTACT =false;
    private EditText edName;
    private ImageView imProfile, imDelete,imSearch;
    private TextView tvKey, tvSave;
    private String sName, sKey, sImage;
    private int contact_id;
    DatabaseProvider databaseProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        init();

        if (!ISNEWCONTACT)
        {
            Intent i = getIntent();
            if (i != null)
            {
                sName = i.getStringExtra("name");
                sKey = i.getStringExtra("key");
                sImage = i.getStringExtra("image");
                contact_id=i.getIntExtra("id",0);
                edName.setText("" + sName);
                tvKey.setText("" + sKey);
                tvSave.setBackgroundColor(R.color.text_back);
                tvSave.setClickable(false);
            }
        }

    }

    private void init()
    {
        databaseProvider=new DatabaseProvider(AddContactActivity.this);
        edName = (EditText) findViewById(R.id.ed_addContact_name);

        imProfile = (ImageView) findViewById(R.id.image_addContact_profile);
        imDelete = (ImageView) findViewById(R.id.image_addContact_delete);
        imSearch= (ImageView) findViewById(R.id.searchnow);
        tvKey = (TextView) findViewById(R.id.tv_addContact_key);
        tvSave = (TextView) findViewById(R.id.tv_addContact_Save);

        imSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key= RandomStringUtils.randomNumeric(6);
                tvKey.setText(key);
            }
        });
        tvSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Contact contact=new Contact(0,edName.getText().toString(),null,tvKey.getText().toString());
                DatabaseProvider databaseProvider=new DatabaseProvider(AddContactActivity.this);
                databaseProvider.addContact(contact);
                MainActivity.mMainActivity.doRegisterContacts(edName.getText().toString());
                ContactActivity.contactActivity.notifyAddedContact(contact);
                AddContactActivity.this.finish();
            }
        });

        imDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!ISNEWCONTACT)
                {
                    Log.v("imDelete","removing contact...");
                    databaseProvider.removeContact(contact_id);
                    ContactActivity.contactActivity.notifyRemovedContact(ContactAdapter.currentContact);
                }
                AddContactActivity.this.finish();
            }
        });
    }
}
