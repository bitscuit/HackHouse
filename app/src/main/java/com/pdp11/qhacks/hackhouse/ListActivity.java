package com.pdp11.qhacks.hackhouse;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

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

public class ListActivity extends AppCompatActivity {

    // list of items
    ArrayList<String> items;
    ItemAdapter adapter;
    ListView lView;

    private DatabaseReference mDatabaseList;
    private DatabaseReference mDatabaseUser;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        items = new ArrayList<>();
        adapter = new ItemAdapter(items, this);
        lView = (ListView) findViewById(R.id.item_list_view);
        lView.setAdapter(adapter);

        Intent extras = getIntent();
        final String todoTitle = extras.getStringExtra("todoTitle");

        // Gets the user email. Not sure what assert does, but the login should have been correct in order to get to this screen.
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mFirebaseUser != null;
        final String name = mFirebaseUser.getDisplayName();

        mDatabaseList = FirebaseDatabase.getInstance().getReference().child("List Titles").child(todoTitle);
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(name);

        ValueEventListener todoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Loop over each child in the root branch.
                if (items.size() != 0) {
                    items.clear();
                }
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    items.add(snap.getKey());
                } // end for loop
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseList.addValueEventListener(todoListener);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        // add item to list
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pop-up dialog box for user to enter the to-do name
                final AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
                builder.setTitle("Enter Item");
                // Set up the input
                final EditText input = new EditText(ListActivity.this);
                // Specify the type of input expected
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // The next few lines gives the input area the focus so the user can type. It also brings up the keyboard.
                input.requestFocus();
                final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                // Set up the buttons
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String addedItem = input.getText().toString();
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put(addedItem, 0);
                        mDatabaseList.updateChildren(itemMap);

                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0); // Hides keyboard
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        dialog.cancel();    // User clicks cancel, dialog box goes away
                    }
                });
                builder.show(); // Shows the dialog box.
            }
        });

        // Delete list item that is clicked
        lView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int pos, final long id) {
                final AlertDialog.Builder b = new AlertDialog.Builder(ListActivity.this);
                b.setIcon(android.R.drawable.ic_dialog_alert);
                b.setMessage("Delete?");
                b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String todoItem = lView.getItemAtPosition(pos).toString();
                        mDatabaseList.child(todoItem).removeValue();
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
    } // end onCreate method



} // end ListActivity