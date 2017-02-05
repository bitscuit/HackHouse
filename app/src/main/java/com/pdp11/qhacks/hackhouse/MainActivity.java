package com.pdp11.qhacks.hackhouse;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> todoList;
    private TodoAdapter adapter;
    private ListView lView;
    private String todoTitle;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;
    private String mPhotoUrl;
    private DatabaseReference mDatabaseUser;
    private DatabaseReference mDatabaseList;
    private DatabaseReference mDatabaseRoot;

    private ArrayList<String> collabList;
    private String[] args;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeFirebase();

        todoList = new ArrayList<>();   // todoList Titles
        adapter = new TodoAdapter(todoList, this);
        lView = (ListView) findViewById(R.id.todo_list_view);
        lView.setAdapter(adapter);

        // Gets the user's google account name. Not sure what assert does, but the login should have been correct in order to get to this screen.
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mFirebaseUser != null;
        final String name = mFirebaseUser.getDisplayName();
        assert mFirebaseUser != null;
        final String email = mFirebaseUser.getEmail();

        // Get user account name and use that as a database document title
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(name);
        mDatabaseList = FirebaseDatabase.getInstance().getReference().child("List Titles");
        mDatabaseRoot = FirebaseDatabase.getInstance().getReference();

        mDatabaseUser.child("Email").setValue(email);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    // Pop-up dialog box for user to enter the to-do name
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Enter Title of TODO");
                    // Set up the input
                    final EditText input = new EditText(MainActivity.this);
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
                            todoTitle = input.getText().toString() + ";" + name;

                            if (todoTitle.contains(";")) {
                                Toast.makeText(MainActivity.this, "Try again without the following character: \";\"", Toast.LENGTH_LONG).show();
                            } else {
                                mDatabaseUser.child("Todo List").child(todoTitle).setValue(0);     // Adds the todoTitle to the User's document
                                mDatabaseList.child(todoTitle).child("List Items").setValue(0);	// Adds list item branch whith no list items
                                mDatabaseList.child(todoTitle).child("Collaborators").child(name).setValue(0); // Adds collaborators branch wiht the username as defualt
                            }
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

        // Reads database on a data change
        ValueEventListener todoListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Loop over each child in the user's account branch.
                if (todoList.size() != 0) {
                    todoList.clear();
                }
                for (DataSnapshot snap : dataSnapshot.child("Todo List").getChildren()) {
                    args = snap.getKey().split(";");
                    todoList.add(args[0]);    // Adds "Users -> Michael Tanel -> 458, Assignment3" for example
                } // end for loop
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseUser.addValueEventListener(todoListener);

        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Intent intent = new Intent(MainActivity.this, ListActivity.class);
                intent.putExtra("todoTitle", lView.getItemAtPosition(pos).toString() + ";" + args[1]);  // pass the todoTitle into ListActivity
                startActivity(intent);
            }
        });

        // Delete list item that is clicked
        lView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int pos, final long id) {
                final AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setIcon(android.R.drawable.ic_dialog_alert);
                b.setMessage("Delete?");
                b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        todoTitle = lView.getItemAtPosition(pos).toString();
//                        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(name);
//                        mDatabaseList = FirebaseDatabase.getInstance().getReference().child("List Titles");

                        collabList = new ArrayList<>();
                        // Adds collaborator in "List Titles -> TodoList" and adds TodoList in Users

                        mDatabaseRoot.child("List Titles").child(todoTitle + ";" + name).child("Collaborators").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                boolean isDeleted = false;
                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    // Retrieves a list of collaborators
                                    collabList.add(snapshot.getKey());
                                    mDatabaseRoot.child("Users").child(snapshot.getKey()).child("Todo List").child(todoTitle + ";" + name).removeValue();
                                    isDeleted = true;
                                }
                                if (isDeleted) {
                                    mDatabaseUser.child("Todo List").child(todoTitle + ";" + name).removeValue();
                                    mDatabaseList.child(todoTitle + ";" + name).removeValue();
                                }

                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


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

    private void initializeFirebase() {
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }
    } // end initializeFirebase method

}
