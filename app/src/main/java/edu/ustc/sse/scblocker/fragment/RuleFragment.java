package edu.ustc.sse.scblocker.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.ustc.sse.scblocker.R;
import edu.ustc.sse.scblocker.activity.MainActivity;
import edu.ustc.sse.scblocker.activity.RuleEditActivity;
import edu.ustc.sse.scblocker.adapter.RuleAdapter;
import edu.ustc.sse.scblocker.model.Rule;
import edu.ustc.sse.scblocker.util.BlockManager;
import io.github.codefalling.recyclerviewswipedismiss.SwipeDismissRecyclerViewTouchListener;

/**
 * Created by dc on 000012/6/12.
 */
public class RuleFragment extends Fragment  {

    public static final int REQUEST_CODE = 0;

    private MainActivity mMainActivity;
    private RecyclerView mRecyclerView;
    private RuleAdapter mRuleAdapter;

    private BlockManager mBlockManager;

    private int positionUpdated = -1;
    private Rule ruleUpdated = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainActivity = (MainActivity) getActivity();
        mBlockManager = new BlockManager(mMainActivity);
        setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rule, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_rules);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mMainActivity));


        mRuleAdapter = new RuleAdapter(mMainActivity, mBlockManager.getRules(BlockManager.TYPE_ALL));
        mRecyclerView.setAdapter(mRuleAdapter);

        SwipeDismissRecyclerViewTouchListener listener = new SwipeDismissRecyclerViewTouchListener.Builder(
                mRecyclerView,
                new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        if (position < 0) {
                            return false;
                        }
                        return true;
                    }

                    @Override
                    public void onDismiss(View view) {
                        int id = mRecyclerView.getChildPosition(view);
                        Rule rule = mRuleAdapter.getItem(id);
                        mRuleAdapter.delete(id);
                        mBlockManager.deleteRule(rule);
                        Snackbar.make(mRecyclerView, String.format("Rule item %d", id), Snackbar.LENGTH_LONG).show();
                    }
                })
                .setIsVertical(false)
                .setItemClickCallback(
                        new SwipeDismissRecyclerViewTouchListener.OnItemClickCallBack() {
                            @Override
                            public void onClick(int position) {
                                if (position < 0) {
                                    return;
                                }
                                positionUpdated = position;
                                Rule rule = mRuleAdapter.getItem(position);
                                ruleUpdated = rule;
                                if (rule != null) {
                                    Intent intent = RuleEditActivity.newIntent(mMainActivity,
                                            RuleEditActivity.OPERATION_MODIFY);
                                    intent.putExtra("rule", rule);
                                    intent.putExtra("position", position);
                                    startActivityForResult(intent, REQUEST_CODE);
                                }
                            }
                        }
                )
                .setItemTouchCallback(
                        new SwipeDismissRecyclerViewTouchListener.OnItemTouchCallBack() {
                            @Override
                            public void onTouch(int position) {

                            }
                        }
                )
                .create();

        mRecyclerView.setOnTouchListener(listener);

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        Bundle bundle = data.getExtras();
        Rule rule = (Rule) bundle.get("rule");
        int position = bundle.getInt("position");
        if (position == -1) {
            mRuleAdapter.add(0, rule);
        } else {
            mRuleAdapter.replace(position, rule);
        }


    }


}
