package edu.ustc.sse.scblocker.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Date;

import edu.ustc.sse.scblocker.R;
import edu.ustc.sse.scblocker.activity.MainActivity;
import edu.ustc.sse.scblocker.adapter.BlockContentAdapter;
import edu.ustc.sse.scblocker.model.BlockContent;
import edu.ustc.sse.scblocker.util.BlockManager;
import io.github.codefalling.recyclerviewswipedismiss.SwipeDismissRecyclerViewTouchListener;

/**
 * Created by dc on 000011/6/11.
 */
public class BlockContentFragment extends Fragment {

    private static final String ARG_BLOCK_CONTENT_TYPE = "type";

    private MainActivity mMainActivity;
    private RecyclerView mRecyclerView;

    private BlockContentAdapter mAdapter;

    private int type;

    public static BlockContentFragment newInstance(int type){
        BlockContentFragment fragment = new BlockContentFragment();
        Bundle args = new Bundle();

        args.putInt(ARG_BLOCK_CONTENT_TYPE, type);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainActivity = (MainActivity) getActivity();

        this.type = getArguments().getInt(ARG_BLOCK_CONTENT_TYPE);


        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blockcontent, container, false);

        if (view != null) {
            mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_blockcontent);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(mMainActivity));


            ArrayList<BlockContent> list = setupContent(this.type);
            mAdapter = new BlockContentAdapter(mMainActivity, list);
            mRecyclerView.setAdapter(mAdapter);

            SwipeDismissRecyclerViewTouchListener listener = new SwipeDismissRecyclerViewTouchListener.Builder(
                    mRecyclerView,
                    new SwipeDismissRecyclerViewTouchListener.DismissCallbacks(){
                        @Override
                        public boolean canDismiss(int i) {
                            return true;
                        }
                        @Override
                        public void onDismiss(View view) {
                            int id = mRecyclerView.getChildAdapterPosition(view);
                            mAdapter.data.remove(id);
                            //TODO: Database deletion operation
                            mAdapter.notifyDataSetChanged();
                            Snackbar.make(mRecyclerView, String.format("Deleted item %d", id),Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .setIsVertical(false)
                    .setItemTouchCallback(
                            new SwipeDismissRecyclerViewTouchListener.OnItemTouchCallBack(){
                                @Override
                                public void onTouch(int i) {

                                }
                            })
                    .setItemClickCallback(
                            new SwipeDismissRecyclerViewTouchListener.OnItemClickCallBack() {
                                @Override
                                public void onClick(int i) {

                                }
                            })
                    .create();

            mRecyclerView.setOnTouchListener(listener);

        }

        return view;
    }


    // Fill testing data
    private ArrayList<BlockContent> setupContent(int type) {
        ArrayList<BlockContent> contents = new ArrayList<>();
        long now = new Date().getTime();

        switch (type){
            case BlockManager.TYPE_ALL:
                insertCalls(contents, now);
                insertSMSes(contents, now);
                break;
            case BlockManager.TYPE_CALL:
                insertCalls(contents, now);
                break;
            case BlockManager.TYPE_SMS:
                insertSMSes(contents, now);
                break;
        }
        return contents;
    }
    private void insertCalls(ArrayList<BlockContent> contents, long now){
        BlockContent content1 = new BlockContent();
        content1.setNumber("18501567510");
        content1.setType(BlockContent.BLOCK_CALL);
        content1.setContent("");
        content1.setCreated(now);
        content1.setRead(BlockContent.UNREADED);
        contents.add(content1);
        contents.add(content1);

        for (int i = 0; i < 5; i++){
            contents.add(content1);
        }
    }
    private void insertSMSes(ArrayList<BlockContent> contents, long now){
        BlockContent content = new BlockContent();
        content.setNumber("10010");
        content.setType(BlockContent.BLOCK_SMS);
        content.setContent("您已欠费，请及时充值。\n现在您只能接听电话。");
        content.setCreated(now);
        content.setRead(BlockContent.UNREADED);

        contents.add(content);
        contents.add(content);

        content = new BlockContent();
        content.setNumber("15196310245");
        content.setType(BlockContent.BLOCK_SMS);
        content.setContent("这是测试，这是测试，这是测试，这是测试，这是测试，这是测试，这是测试，这是测试，这是测试，");
        content.setCreated(now);
        content.setRead(BlockContent.UNREADED);
        contents.add(content);
        contents.add(content);
        contents.add(content);

        for (int i = 0; i < 5; i++){
            contents.add(content);
        }
    }


}
