package com.example.cs160_sp18.prog3;

import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// Displays a list of comments for a particular landmark.
public class CommentFeedActivity extends AppCompatActivity {

    private static final String TAG = CommentFeedActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<Comment> mComments = new ArrayList<Comment>();
    private Intent commentIntent;

    // UI elements
    EditText commentInputBox;
    RelativeLayout layout;
    Button sendButton;
    Toolbar mActionBarToolbar;

    DatabaseReference landmarkDbRef;
    long numComments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_feed);

        commentIntent = getIntent();

        String landmarkName = commentIntent.getStringExtra("landmarkName");

        mActionBarToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mActionBarToolbar);
        getSupportActionBar().setTitle(landmarkName + ": Posts");


        // hook up UI elements
        layout = (RelativeLayout) findViewById(R.id.comment_layout);
        commentInputBox = (EditText) layout.findViewById(R.id.comment_input_edit_text);
        sendButton = (Button) layout.findViewById(R.id.send_button);


        mRecyclerView = (RecyclerView) findViewById(R.id.comment_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // create an onclick for the send button
        setOnClickForSendButton();

        // get comments from Firebase
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference commentsRef = db.getReference("comments");
        landmarkDbRef = commentsRef.child(landmarkName);
        ValueEventListener myDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mComments = new ArrayList<Comment>();
                HashMap<String, String> landmarkMap = (HashMap<String, String>) dataSnapshot.getValue();
                if(landmarkMap != null){
                    Iterator it = landmarkMap.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();
                        HashMap<String,Object> obj = (HashMap<String, Object>) pair.getValue();
                        long timeStamp = Long.parseLong(obj.get("date").toString());
                        Comment newComment = new Comment(obj.get("text").toString(), obj.get("username").toString(), new Date(timeStamp*1000));
                        mComments.add(newComment);
                        it.remove(); // avoids a ConcurrentModificationException
                    }
                }
                numComments = dataSnapshot.getChildrenCount();
                setAdapterAndUpdateData();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("0", "cancelled");
            }
        };
        landmarkDbRef.addValueEventListener(myDataListener);
    }

    private void setOnClickForSendButton() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = commentInputBox.getText().toString();
                if (TextUtils.isEmpty(comment)) {
                    // don't do anything if nothing was added
                    commentInputBox.requestFocus();
                } else {
                    // clear edit text, post comment
                    commentInputBox.setText("");
                    postNewComment(comment);
                }
            }
        });
    }

    private void setAdapterAndUpdateData() {
        // create a new adapter with the updated mComments array
        // this will "refresh" our recycler view
        Collections.sort(mComments);
        mAdapter = new CommentAdapter(this, mComments);
        mRecyclerView.setAdapter(mAdapter);

        // scroll to the last comment
        if(mComments.size() > 0) {
            mRecyclerView.smoothScrollToPosition(mComments.size() - 1);
        }
    }

    private void postNewComment(String commentText) {
        numComments += 1;
        Comment newComment = new Comment(commentText, commentIntent.getStringExtra("username"), new Date().getTime()/1000L);
        Map<String, Object> commentValues = newComment.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("message"+numComments, commentValues);
        landmarkDbRef.updateChildren(childUpdates);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
