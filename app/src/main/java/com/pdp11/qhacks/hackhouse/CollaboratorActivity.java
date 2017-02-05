package com.pdp11.qhacks.hackhouse;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CollaboratorActivity extends AppCompatActivity {

    private ArrayList<String> items;
    private CollaboratorAdapter adapter;
    private ListView lView;
    private String todoTitle;
    private ArrayList<String> collabList;
    private String myName;
    private String ownerName;

    private DatabaseReference mDatabaseList;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabaseCollab;
    private DatabaseReference mDatabaseRoot;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collaborator);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        items = new ArrayList<>();
        adapter = new CollaboratorAdapter(items, this);
        lView = (ListView) findViewById(R.id.collab_list_view);
        lView.setAdapter(adapter);

        // Gets the user's google account name. Not sure what assert does, but the login should have been correct in order to get to this screen.
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mFirebaseUser != null;
        myName = mFirebaseUser.getDisplayName();

        Intent extras = getIntent();
        String[] args = extras.getStringExtra("todoTitle").split(";");
        todoTitle = args[0];
        ownerName = args[1];

        mDatabaseList = FirebaseDatabase.getInstance().getReference().child("List Titles").child(todoTitle + ";" + ownerName).child("Collaborators");
        mDatabaseRoot = FirebaseDatabase.getInstance().getReference();

        ValueEventListener todoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Loop over each child in the root branch.
                if (items.size() != 0) {
                    items.clear();
                }
//                Log.d("collaborator", "outside for loop: " + dataSnapshot.getKey());
                // Add all of collaborators
                boolean isThere = false;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                    Log.d("collaborator", "In for loop " + snapshot.getKey());
                    if (!(snapshot.getKey().equals(myName))) {
                        items.add(snapshot.getKey());
                    } else {
                        isThere = true;
                    }
                }
                adapter.notifyDataSetChanged();
                if (!isThere) {
                    Intent intent = new Intent();
                    setResult(1002, intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseList.addValueEventListener(todoListener);

//        // Delete list item that is clicked
//        lView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
//                                           final int pos, final long id) {
//                final AlertDialog.Builder b = new AlertDialog.Builder(CollaboratorActivity.this);
//                b.setIcon(android.R.drawable.ic_dialog_alert);
//                b.setMessage("Delete?");
//                b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//                        todoTitle = lView.getItemAtPosition(pos).toString();
//                        collabList = new ArrayList<>();
//                        // Adds collaborator in "List Titles -> TodoList" and adds TodoList in Users
//
//                        mDatabaseRoot.addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                                String collaborator = lView.getItemAtPosition(pos).toString();
//                                mDatabaseRoot.child("List Titles").child(todoTitle + ";" + myName).child("Collaborators").child(collaborator).removeValue();
//                                mDatabaseRoot.child("Users").child(collaborator).child("Todo List").child(todoTitle + ";" + myName).removeValue();
//                            }
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//
//
//                    }
//                });
//                b.setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int whichButton) {
//
//                    }
//                });
//                b.show();
//
//                return true;
//            }
//        });

        // Delete list item that is clicked
        lView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int pos, final long id) {
                final AlertDialog.Builder b = new AlertDialog.Builder(CollaboratorActivity.this);
                b.setIcon(android.R.drawable.ic_dialog_alert);
                b.setMessage("Delete?");
                b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String collab = lView.getItemAtPosition(pos).toString();
                        mDatabaseRoot.child("List Titles").child(todoTitle + ";" + myName).child("Collaborators").child(collab).removeValue();
                        mDatabaseRoot.child("Users").child(collab).child("Todo List").child(todoTitle + ";" + myName).removeValue();
                    }
                });
                b.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                b.show();

                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            Intent intent = new Intent();
            intent.putExtra("todoTitle", todoTitle);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("todoTitle", todoTitle);
        setResult(RESULT_OK, intent);
        finish();
    }

}
