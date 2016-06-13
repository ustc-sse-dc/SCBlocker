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

    private BlockManager mBlockManager;
    private ArrayList<BlockContent> mBlockContents;


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
        mBlockManager = new BlockManager(mMainActivity);
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

            mBlockContents = mBlockManager.getContents(-1, type);
            mAdapter = new BlockContentAdapter(mMainActivity, mBlockContents);
            mRecyclerView.setAdapter(mAdapter);

            SwipeDismissRecyclerViewTouchListener listener = new SwipeDismissRecyclerViewTouchListener.Builder(
                    mRecyclerView,
                    new SwipeDismissRecyclerViewTouchListener.DismissCallbacks(){
                        @Override
                        public boolean canDismiss(int position) {
                            if (position < 0){
                                return false;
                            }
                            return true;
                        }
                        @Override
                        public void onDismiss(View view) {
                            int id = mRecyclerView.getChildPosition(view);
                            BlockContent content = mAdapter.get(id);
                            mAdapter.delete(id);
                            mBlockManager.deleteBlockContent(content);
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



}
