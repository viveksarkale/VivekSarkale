package chat.application.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import chat.application.MainActivity;
import chat.application.R;
import chat.application.activity.AddContactActivity;
import chat.application.activity.ComposeActivity;
import chat.application.activity.ContactActivity;
import chat.application.model.Contact;


@SuppressWarnings("ALL")
public class ContactAdapter extends ArrayAdapter<Contact>
{
    ArrayList<Contact> list;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;
    Context context;
    public static Contact currentContact;

    public ContactAdapter(Context context, int resource, ArrayList<Contact> objects)
    {
        super(context, resource, objects);
        this.context = context;
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        list = objects;
    }

    @Override
    public int getCount()
    {
        return list.size();
    }

    @Override
    public Contact getItem(int position)
    {
        return list.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    /*private boolean ifUserIsOnline(String user) {
        boolean n = false;
        for(int i=0;i<MainActivity.contactStatuses.size();i++) {
            Log.d("useronline",""+MainActivity.contactStatuses.get(i).getUser()+" "+user+" "+MainActivity.contactStatuses.get(i).isStatus());
            if(user.equals(MainActivity.contactStatuses.get(i).getUser())&&(MainActivity.contactStatuses.get(i).isStatus()==true))
                n= true;
        }
        return n;
    }*/

    public void launchNext(Intent intent) {
        context.startActivity(intent);
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent)
    {
        // convert view = design
        View view = convertView;
        if (view == null)
        {
            holder = new ViewHolder();
            view = vi.inflate(Resource, null);

            holder.tvName = (TextView) view.findViewById(R.id.tv_itemContact_name);
            holder.imSetting = (ImageView) view.findViewById(R.id.image_setting_contact);
            holder.loginInfo = (Button) view.findViewById(R.id.login_status) ;

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        final Contact contact=list.get(position);
        holder.tvName.setText(contact.getUsername()+":"+contact.getPublic_key());
        holder.tvName.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("view","clicked");
                ComposeActivity.checkIntent = "Check";
                Intent i = new Intent(context,ComposeActivity.class);
                i.putExtra("senderName",contact.getUsername());
                i.putExtra("subject", "");
                launchNext(i);
            }
        });

        //Log.d("Adapter ", ""+MainActivity.contactStatuses.get(0).isStatus());

        if(contact.getOnline())
            holder.loginInfo.setBackgroundColor(Color.GREEN);
        else
            holder.loginInfo.setBackgroundColor(Color.RED);



        holder.imSetting.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AddContactActivity.ISNEWCONTACT =false;
                Intent i = new Intent(context, AddContactActivity.class);
                i.putExtra("id",list.get(position).getId());
                i.putExtra("name", list.get(position).getUsername());
                i.putExtra("image", list.get(position).getImage());
                i.putExtra("key", list.get(position).getPublic_key());
                currentContact=list.get(position);
                context.startActivity(i);
            }
        });

        return view;
    }
    static class ViewHolder
    {
        protected TextView tvName;
        protected ImageView imSetting;
        protected Button loginInfo;
    }
}