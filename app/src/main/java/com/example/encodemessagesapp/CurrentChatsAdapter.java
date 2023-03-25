package com.example.encodemessagesapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CurrentChatsAdapter extends ArrayAdapter implements View.OnClickListener {

    private Context myContext;
    private ArrayList<CurrentChatsModel> currentChatsArrayList;
    int resource;

    public CurrentChatsAdapter(@NonNull Context context, int resource, @NonNull ArrayList objects) {
        super(context, resource, objects);

        this.resource = resource;
        this.currentChatsArrayList = objects;
        this.myContext = context;
    }

    @Override
    public void onClick(View v) {
        String contactNumber = (String) v.getTag(R.string.contactNumber);
        TextView txtSelectedItem_contact = v.findViewById(R.id.txtContact);
        String contactName = txtSelectedItem_contact.getText().toString();

        if (txtSelectedItem_contact.getTypeface().isBold()) {
            MySqliteDB mySqliteDB = new MySqliteDB(myContext);
            mySqliteDB.UpdateSmsReadStatus(contactNumber);
        }

        //start contact chats on clicking on his/her item
        Intent intent = new Intent(myContext, ContactChatActivity.class);
        intent.putExtra("contactName", contactName);
        intent.putExtra("contactNumber", contactNumber);
        myContext.startActivity(intent);
    }


    class ViewHolder {
        TextView txtContact, txtLastSmsDate, txtLastSms;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView;
        ViewHolder holder;
        CurrentChatsModel currentChatsModel = currentChatsArrayList.get(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(myContext);
            convertView = inflater.inflate(resource, parent, false);
            holder = new ViewHolder();

            holder.txtContact = convertView.findViewById(R.id.txtContact);
            holder.txtLastSmsDate = convertView.findViewById(R.id.txtLastSmsDate);
            holder.txtLastSms = convertView.findViewById(R.id.txtLastSms);


            itemView = convertView;
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
            itemView = convertView;
        }

        holder.txtContact.setText(currentChatsModel.getContact());


        Date smsDate = null;
        try {
            smsDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(currentChatsModel.getLastSmsDate());
            //smsDate = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(currentChatsModel.getLastSmsDate());

            String day = (String) DateFormat.format("dd",   smsDate);
            String month  = (String) DateFormat.format("MM",   smsDate);
            String year         = (String) DateFormat.format("yyyy", smsDate);
            String minute  = (String) DateFormat.format("mm",   smsDate);
            String hour         = (String) DateFormat.format("HH", smsDate);
            String sSmsDate = year + "-" + month + "-" + day + " " + hour + ":" + minute;

            holder.txtLastSmsDate.setText(sSmsDate);
            if (currentChatsModel.getLastSms().length() > 45)
                holder.txtLastSms.setText(currentChatsModel.getLastSms().substring(0, 42) + "...");
            else
                holder.txtLastSms.setText(currentChatsModel.getLastSms());

            if (currentChatsModel.getIsRead() == 0)
                holder.txtContact.setTypeface(holder.txtContact.getTypeface(), Typeface.BOLD);
            else
                holder.txtContact.setTypeface(holder.txtContact.getTypeface(), Typeface.NORMAL);

            itemView.setTag(R.string.contactNumber, currentChatsModel.getContactNumber());
            itemView.setOnClickListener(this);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return itemView;
    }
}
