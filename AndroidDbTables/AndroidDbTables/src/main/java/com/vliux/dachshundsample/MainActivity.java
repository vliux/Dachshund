package com.vliux.dachshundsample;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.vliux.dachshund.BaseDbTable;
import com.vliux.dachshund.DbManager;

import java.util.List;

/**
 * Created by vliux on 12/6/13.
 */
public class MainActivity extends Activity {
    private TextView mDbContentTv;
    private Button mBtnRefresh;
    private Button mBtnAdd;
    private SampleTable mSampleTable;
    private SampleTableEx mSampleTableEx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDbContentTv = (TextView)findViewById(R.id.table_db);
        mBtnRefresh = (Button)findViewById(R.id.btn_refresh);
        mBtnAdd = (Button)findViewById(R.id.btn_add);

        try {
            DbManager.init(getApplicationContext(),new Class[]{SampleTable.class, SampleTableEx.class}, "SampleDb", 5);
            mSampleTable = (SampleTable)DbManager.getsInstance().getTable(SampleTable.class);
            mSampleTableEx = (SampleTableEx)DbManager.getsInstance().getTable(SampleTableEx.class);
            mSampleTableEx.query(null, null);
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTable();
    }

    public void onClick(View view){
        if(null != mSampleTable){
            switch(view.getId()){
                case R.id.btn_add:
                    mSampleTable.addRandomRow();
                    break;
                case R.id.btn_refresh:
                    refreshTable();
                    break;
            }
        }
    }

    private void refreshTable(){
        if(null == mSampleTable){
            return;
        }

        StringBuilder sb = new StringBuilder();
        List<ContentValues> resultValues = mSampleTable.query(null, null);
        for(ContentValues cv : resultValues){
            sb.append(cv.toString());
            sb.append("\n");
        }

        mDbContentTv.setText(sb.toString());
    }
}
