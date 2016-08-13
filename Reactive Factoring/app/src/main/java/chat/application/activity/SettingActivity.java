package chat.application.activity;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import chat.application.MainActivity;
import chat.application.R;

public class SettingActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        EditText serverAddress = (EditText)findViewById(R.id.server_address);
        EditText userName = (EditText) findViewById(R.id.user_name);
        EditText publicKey = (EditText) findViewById(R.id.public_key);

        serverAddress.setText(LoginActivity.serverName);
        userName.setText(LoginActivity.uName);
        publicKey.setText(LoginActivity.myCrypto.getPublicKeyString());
    }
}
