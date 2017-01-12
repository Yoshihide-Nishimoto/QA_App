package jp.techacademy.yoshihide.nishimoto.qa_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class QuestionDetailListAdapter extends BaseAdapter {

    private final static int TYPE_QUESTION = 0;
    private final static int TYPE_ANSWER = 1;


    private LayoutInflater mLayoutInflater = null;
    private Question mQustion;
    private ImageView mfavorite;
    private Boolean mfavorite_flg=false;

    private int mGenre;
    private String uid;
    DatabaseReference mDataBaseReference;
    private DatabaseReference mData;
    private DatabaseReference mUserData;

    private SharedPreferences sp;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Log.d("Android","判定開始");
            if (dataSnapshot.getKey().equals(mQustion.getQuestionUid())){
                Log.d("Android","Trueに変更");
                mfavorite_flg = true;
                mfavorite.setImageResource(R.drawable.favorite_on);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

            Log.d("Android","判定開始");
            if (dataSnapshot.getKey().equals(mQustion.getQuestionUid())){
                Log.d("Android","falseに変更");
                mfavorite_flg = false;
                mfavorite.setImageResource(R.drawable.favorite_off);
            }

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public QuestionDetailListAdapter(Context context, Question question,int genre,String uid) {
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.sp = context.getSharedPreferences("favoritelist",MODE_PRIVATE);
        mQustion = question;
        mGenre = genre;
        this.uid = uid;
    }

    @Override
    public int getCount() {
        return 1 + mQustion.getAnswers().size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_QUESTION;
        } else {
            return TYPE_ANSWER;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return mQustion;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_question_detail, parent, false);
            }

            String body = mQustion.getBody();
            String name = mQustion.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);

            byte[] bytes = mQustion.getImageBytes();
            if (bytes.length != 0) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
                imageView.setImageBitmap(image);
            }

            mDataBaseReference = FirebaseDatabase.getInstance().getReference();
            mData = mDataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre)).child(String.valueOf(mQustion.getQuestionUid()));
            mUserData = mDataBaseReference.child(Const.UsersPATH).child(uid);

            mUserData.child(Const.FavoritePATH).addChildEventListener(mEventListener);

            mfavorite = (ImageView) convertView.findViewById(R.id.imageView2);
            mfavorite.setImageResource(R.drawable.favorite_off);

            mfavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    SharedPreferences.Editor editor = sp.edit();

                    Map<String, Object> favorite = new HashMap<>();
                    if (mfavorite_flg) {
                        mUserData.child(Const.FavoritePATH).child(mQustion.getQuestionUid()).removeValue();
                        mfavorite.setImageResource(R.drawable.favorite_off);
                        editor.putString(mQustion.getQuestionUid(), "false");
                    } else {
                        favorite.put(mQustion.getQuestionUid(), "true");
                        mUserData.child(Const.FavoritePATH).updateChildren(favorite);
                        mfavorite.setImageResource(R.drawable.favorite_on);
                        editor.putString(mQustion.getQuestionUid(), "true");
                    }
                    editor.commit();

                }
            });

        } else {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_answer, parent, false);
            }

            Answer answer = mQustion.getAnswers().get(position - 1);
            String body = answer.getBody();
            String name = answer.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);
        }

        return convertView;
    }
}