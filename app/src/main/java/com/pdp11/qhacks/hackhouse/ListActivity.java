package com.pdp11.qhacks.hackhouse;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
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

public class ListActivity extends AppCompatActivity {

    // list of items
    private ArrayList<String> items;
    private ItemAdapter adapter;
    private ListView lView;
    private String todoTitle;
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
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        items = new ArrayList<>();
        adapter = new ItemAdapter(items, this);
        lView = (ListView) findViewById(R.id.item_list_view);
        lView.setAdapter(adapter);

        // Gets the user's google account name. Not sure what assert does, but the login should have been correct in order to get to this screen.
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mFirebaseUser != null;
        myName = mFirebaseUser.getDisplayName();

        Intent extras = getIntent();
        String[] extra = extras.getStringExtra("todoTitle").split(";");
        todoTitle = extra[0];
        ownerName = extra[1];

        mDatabaseList = FirebaseDatabase.getInstance().getReference().child("List Titles").child(todoTitle + ";" + ownerName).child("List Items");
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("Users").child(ownerName).child("Todo List");
        mDatabaseCollab = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseRoot = FirebaseDatabase.getInstance().getReference().child("List Titles").child(todoTitle + ";" + ownerName).child("Collaborators");

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

        ValueEventListener todoListener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Add all of collaborators
                boolean isThere = false;
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    if (snap.getKey().equals(myName)) {
                        isThere = true;
                    }
                } // end for loop
                adapter.notifyDataSetChanged();

                if (!isThere) {
                    finish();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseRoot.addValueEventListener(todoListener2);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_collaborator) {
            // Pop-up dialog box for user to enter the to-do name
            final AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this);
            builder.setTitle("Enter Collaborator");
            // Set up the input
            final EditText input = new EditText(ListActivity.this);
            // Specify the type of input expected
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            // The next few lines gives the input area the focus so the user can type. It also brings up the keyboard.
            input.requestFocus();
            final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            // Add collaborator button
            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String collab = input.getText().toString().trim();

                    // Adds collaborator in "List Titles -> TodoList" and adds TodoList in Users
                    mDatabaseCollab.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            boolean nameFound = false;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.getKey().trim().equals(collab)) {

                                    // Adds todoTitle to the Users TodoList
                                    Log.d("collaborator", "Inside for loop " + snapshot.getKey());
                                    Map<String, Object> itemMap = new HashMap<>();
                                    itemMap.put(todoTitle + ";" + ownerName, 0);
                                    mDatabaseCollab.child(collab).child("Todo List").updateChildren(itemMap);

                                    // Adds the collaborator name to the list title
                                    Map<String, Object> todoItemMap = new HashMap<>();
                                    todoItemMap.put(collab, 0);
                                    mDatabaseRoot.updateChildren(todoItemMap);
                                    nameFound = true;
                                }
                            }
                            // Flag checks if the username is found
                            if (!nameFound) {
                                Toast.makeText(ListActivity.this, "This is not a valid username", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

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
            return true;
        } else if (id == R.id.action_view_collaborator) {
            Intent iCollab = new Intent(ListActivity.this, CollaboratorActivity.class);
            iCollab.putExtra("todoTitle", todoTitle + ";" + ownerName);
            startActivityForResult(iCollab, 1001);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("requestCode", requestCode + " " + resultCode);
        if (requestCode == 1001) {
            if(resultCode == RESULT_OK) {
                // Gets the user's google account name. Not sure what assert does, but the login should have been correct in order to get to this screen.
                mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                assert mFirebaseUser != null;
                String name = mFirebaseUser.getDisplayName();
                todoTitle = data.getStringExtra("todoTitle");
            } else if (resultCode == 1002) {
                finish();
            }
        }
    }

} // end ListActivity