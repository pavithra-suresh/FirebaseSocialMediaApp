package com.example.firebasesocialmediaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ViewPostsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private ListView postsListView;
    private ArrayList<String> usernames;
    private ArrayAdapter mAdapter;
    private FirebaseAuth mFirebaseAuth;
    private ImageView sentPostImageView;
    private TextView txtDescription;

    private ArrayList<DataSnapshot> mDataSnapshots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_posts);

        sentPostImageView = findViewById(R.id.sentPostImageView);
        txtDescription = findViewById(R.id.txtDescription);

        mFirebaseAuth = FirebaseAuth.getInstance();

        postsListView = findViewById(R.id.postsListView);
        usernames = new ArrayList<>();
        mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, usernames);
        postsListView.setAdapter(mAdapter);
        mDataSnapshots = new ArrayList<>();
        postsListView.setOnItemClickListener(this);
        postsListView.setOnItemLongClickListener(this);

        FirebaseDatabase.getInstance().getReference().child("my_users").child(mFirebaseAuth.getCurrentUser().getUid()).child("received_posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {

                mDataSnapshots.add(snapshot);

                String fromWhomUsername = (String) snapshot.child("fromWhom").getValue();
                usernames.add(fromWhomUsername);
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                int i = 0;
                for (DataSnapshot snapshot : mDataSnapshots) {
                    if (snapshot.getKey().equals(dataSnapshot.getKey())) {
                        mDataSnapshots.remove(i);
                        usernames.remove(i);
                    }
                    i++;
                }
                mAdapter.notifyDataSetChanged();
                sentPostImageView.setImageResource(R.drawable.island);
                txtDescription.setText("");

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        DataSnapshot myDataSnapShot = mDataSnapshots.get(position);
        String downloadLink = (String) myDataSnapShot.child("imageLink").getValue();

        Picasso.get().load(downloadLink).into(sentPostImageView);
        txtDescription.setText((String) myDataSnapShot.child("des").getValue());

    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
                builder.setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                FirebaseStorage.getInstance().getReference()
                                        .child("my_images").child((String) mDataSnapshots
                                        .get(position).child("imageIdentifier").getValue())
                                        .delete();

                                FirebaseDatabase.getInstance().getReference()
                                        .child("my_users").child(mFirebaseAuth.getCurrentUser()
                                        .getUid()).child("received_posts")
                                        .child(mDataSnapshots.get(position).getKey()).removeValue();

                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            return false;
    }
}