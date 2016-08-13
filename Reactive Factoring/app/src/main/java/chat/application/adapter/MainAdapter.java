package chat.application.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import chat.application.R;
import chat.application.activity.ReadMessageActivity;
import chat.application.model.MainObject;
import chat.application.model.Message;


@SuppressWarnings("ALL")
public class MainAdapter extends ArrayAdapter<Message>
{
    ArrayList<Message> list;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;
    Context context;

    public MainAdapter(Context context, int resource, ArrayList<Message> objects)
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
    public Message getItem(int position)
    {
        return list.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
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

            holder.tvSenderName = (TextView) view.findViewById(R.id.tv_itemMain_senderName);
            holder.tvSubject = (TextView) view.findViewById(R.id.tv_itemMain_subject);
            /*holder.tvRead = (TextView) view.findViewById(R.id.tv_itemMain_read);*/

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.tvSenderName.setText(list.get(position).getUsername());
        holder.tvSubject.setText(list.get(position).getSubject());

        return view;
    }
    static class ViewHolder
    {
        protected TextView tvSenderName, tvSubject/*, tvRead*/;
    }
}