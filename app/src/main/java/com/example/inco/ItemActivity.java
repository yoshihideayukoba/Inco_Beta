package com.example.inco;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;

public class ItemActivity extends AppCompatActivity implements ListView.OnItemLongClickListener {
    public FirebaseUser user;
    public String uid;

    public FirebaseAuth mAuth;

    public FirebaseDatabase database;
    public DatabaseReference reference;

    public CustomAdapter mCustomAdapter;
    public ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        //ログイン情報を取得
        user = FirebaseAuth.getInstance().getCurrentUser();

        //user id = Uid を取得する
        uid = user.getUid();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users").child(uid);

        mListView = (ListView) findViewById(R.id.list_view);

        //CustomAdapterをセット
        mCustomAdapter = new CustomAdapter(getApplicationContext(), R.layout.card_view, new ArrayList<ItemData>());
        mListView.setAdapter(mCustomAdapter);

        //LongListenerを設定
        mListView.setOnItemLongClickListener(this);

        //firebaseと同期するリスナー
        reference.addChildEventListener(new ChildEventListener() {
            //            データを読み込むときはイベントリスナーを登録して行う。
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                アイテムのリストを取得するか、アイテムのリストへの追加がないかリッスンします。
                ItemData itemData = dataSnapshot.getValue(ItemData.class);
                mCustomAdapter.add(itemData);
                mCustomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                リスト内のアイテムに対する変更がないかリッスンします。

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                リストから削除されるアイテムがないかリッスンします。
                Log.d("ItemActivity", "onChildRemoved:" + dataSnapshot.getKey());
                ItemData result = dataSnapshot.getValue(ItemData.class);
                if (result == null) return;

                ItemData item = mCustomAdapter.getItemDataKey(result.getFirebaseKey());

                mCustomAdapter.remove(item);
                mCustomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                並べ替えリストの項目順に変更がないかリッスンします。
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//                ログを記録するなどError時の処理を記載する。
            }
        });

    }

    public void addButton(View v) {
        Intent intent = new Intent(this, AddActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final ItemData itemData = mCustomAdapter.getItem(position);
        uid = user.getUid();

        new AlertDialog.Builder(this)
                .setTitle("Done?")
                .setMessage("この項目を完了しましたか？")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // OK button pressed
                        reference.child(itemData.getFirebaseKey()).removeValue();
//                        mCustomAdapter.remove(itemData);
                    }
                })
                .setNegativeButton("No", null)
                .show();

        return false;
    }

    public void logout(View v) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();

        Intent intent = new Intent(ItemActivity.this, LoginActivity.class);
        intent.putExtra("check", true);
        startActivity(intent);
        finish();

    }
}
