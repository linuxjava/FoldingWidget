package xiao.free.folding;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zhy.base.adapter.ViewHolder;
import com.zhy.base.adapter.recyclerview.CommonAdapter;
import com.zhy.base.adapter.recyclerview.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

import xiao.free.folding.view.StickyNavLayout;

/**
 * Created by robincxiao on 2018/8/31.
 */

public class RecycViewActivity extends FragmentActivity {
    private StickyNavLayout mStickyNavLayout;
    private RecyclerView mRecyclerView;
    private List<String> mDatas = new ArrayList<String>();
    private CommonAdapter mCommonAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycview);

        mStickyNavLayout = (StickyNavLayout) findViewById(R.id.stickynavlayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.id_stickynavlayout_recycview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        for (int i = 0; i < 50; i++) {
            mDatas.add("item -> " + i);
        }

        mStickyNavLayout.setEnablePullDown(false);
        mStickyNavLayout.setListener(new StickyNavLayout.ScrollListener() {
            @Override
            public void onScroll(float percentage) {
                Log.d("xiao1", "percentage=" + percentage);
            }
        });

        mCommonAdapter = new CommonAdapter<String>(this, R.layout.item, mDatas) {
            @Override
            public void convert(ViewHolder holder, String o) {
                holder.setText(R.id.id_info, o);
            }
        };

        mCommonAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(ViewGroup viewGroup, View view, Object o, int i) {
                Toast.makeText(RecycViewActivity.this, "" + i, Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean onItemLongClick(ViewGroup viewGroup, View view, Object o, int i) {
                return false;
            }
        });
        mRecyclerView.setAdapter(mCommonAdapter);

    }

    public void OnExpand(View view){
        mStickyNavLayout.expand(500);
    }

}
