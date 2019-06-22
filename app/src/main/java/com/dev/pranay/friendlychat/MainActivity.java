package com.dev.pranay.friendlychat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener {
    public static final String ANONYMOUS = "anonymous";
    public static final int MESSAGE_LENGTH_LIMIT = 1000;
    public static final int MESSAGE_RESOURCE = R.layout.each_message;
    public static final int RC_SIGN_IN = 1;

    private static final String TAG = "MainActivity";
    private ListView messagesListView;
    private ImageButton addImageButton;
    private EditText messageEditText;
    private Button sendButton;
    private ProgressBar progressBar;
    private String username;
    private List<Message> messageList;
    private MesssageAdapter messsageAdapter;

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference messagesDatabaseReference;
    private ChildEventListener childEventListener;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = ANONYMOUS;

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        messagesDatabaseReference = firebaseDatabase.getReference().child("messages");

        messagesListView = findViewById(R.id.lvMessages);
        addImageButton = findViewById(R.id.ibAddImage);
        messageEditText = findViewById(R.id.etMessage);
        sendButton = findViewById(R.id.bSend);
        progressBar = findViewById(R.id.pbProgressbar);

        messageList = new ArrayList<>();
        messsageAdapter = new MesssageAdapter(this, MESSAGE_RESOURCE, messageList);
        messagesListView.setAdapter(messsageAdapter);

        sendButton.setEnabled(false);
        sendButton.setOnClickListener(this);

        progressBar.setVisibility(ProgressBar.INVISIBLE);

        messageEditText.addTextChangedListener(this);
        messageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MESSAGE_LENGTH_LIMIT)});

        addImageButton.setOnClickListener(this);


        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user == null) {
                    //user not signed in
                    onSignedOut();
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build());

                    startActivityForResult(AuthUI
                                    .getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build()
                            , RC_SIGN_IN);
                } else {
                    //user signed in
                    onSignedIn(user.getDisplayName());
                }
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
        messsageAdapter.clear();
        detachDatabaseReadListener();
    }

    public void attachDatabaseReadListener() {
        if (childEventListener == null) {
            childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Message message = dataSnapshot.getValue(Message.class);
                    messsageAdapter.add(message);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            messagesDatabaseReference.addChildEventListener(childEventListener);
        }
    }

    public void detachDatabaseReadListener() {
        if(childEventListener != null){
            messagesDatabaseReference.removeEventListener(childEventListener);
            childEventListener = null;
        }
    }

    public void onSignedIn(String username) {
        this.username = username;
        attachDatabaseReadListener();
    }

    public void onSignedOut() {
        this.username = ANONYMOUS;
        messsageAdapter.clear();
        detachDatabaseReadListener();
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.toString().trim().length() > 0)
            sendButton.setEnabled(true);
        else
            sendButton.setEnabled(false);
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bSend:
                Message message = new Message(messageEditText.getText().toString(), username, null);
                messagesDatabaseReference.push().setValue(message);
                messageEditText.getText().clear();
                break;
            case R.id.ibAddImage:
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_CANCELED){
                finish();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_signout:
                //onSignedOut();
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
