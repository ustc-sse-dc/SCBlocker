package edu.ustc.sse.scblocker.fragment;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.ustc.sse.scblocker.R;
import edu.ustc.sse.scblocker.activity.MainActivity;
import edu.ustc.sse.scblocker.activity.RuleEditActivity;
import edu.ustc.sse.scblocker.adapter.RuleAdapter;
import edu.ustc.sse.scblocker.model.Rule;
import io.github.codefalling.recyclerviewswipedismiss.SwipeDismissRecyclerViewTouchListener;

/**
 * Created by dc on 000012/6/12.
 */
public class RuleFragment extends Fragment{

    private MainActivity mMainActivity;
    private RecyclerView mRecyclerView;
    private RuleAdapter mRuleAdapter;

    private List<Rule> mRules;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainActivity = (MainActivity)getActivity();
        setHasOptionsMenu(true);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rule, container, false);

        mRecyclerView = (RecyclerView)view.findViewById(R.id.rv_rules);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mMainActivity));

        mRules = setupRules();

        mRuleAdapter = new RuleAdapter(mMainActivity, mRules);
        mRecyclerView.setAdapter(mRuleAdapter);

        SwipeDismissRecyclerViewTouchListener listener = new SwipeDismissRecyclerViewTouchListener.Builder(
                mRecyclerView,
                new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int i) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view) {
                        int id = mRecyclerView.getChildAdapterPosition(view);
                        mRules.remove(id);
                        mRuleAdapter.notifyDataSetChanged();
                        Snackbar.make(mRecyclerView, String.format("Rule item %d", id),Snackbar.LENGTH_LONG).show();
                    }
                })
                .setIsVertical(false)
                .setItemClickCallback(
                        new SwipeDismissRecyclerViewTouchListener.OnItemClickCallBack() {
                            @Override
                            public void onClick(int position) {
                                if (position < 0){
                                    return;
                                }
                                Rule rule = mRules.get(position);
                                if (rule != null){
                                    Intent intent = RuleEditActivity.newIntent(mMainActivity,
                                            RuleEditActivity.OPERATION_MODIFY);
                                    intent.putExtra("rule", rule);
                                    startActivity(intent);
                                }
                            }
                        }
                )
                .setItemTouchCallback(
                        new SwipeDismissRecyclerViewTouchListener.OnItemTouchCallBack() {
                            @Override
                            public void onTouch(int i) {

                            }
                        }
                )
                .create();

        mRecyclerView.setOnTouchListener(listener);

        return view;
    }

    private ArrayList<Rule> setupRules(){
        ArrayList<Rule> list = new ArrayList<>();

        long now = new Date().getTime();

        Rule rule = new Rule();
        rule.setContent("10010");
        rule.setType(Rule.TYPE_STRING);
        rule.setCall(1);
        rule.setSms(1);
        rule.setException(0);
        rule.setRemark("中国联通");
        rule.setCreated(now);
        list.add(rule);

        rule = new Rule();
        rule.setContent("10086");
        rule.setType(Rule.TYPE_STRING);
        rule.setCall(1);
        rule.setSms(0);
        rule.setException(0);
        rule.setRemark("中国移动");
        rule.setCreated(now);
        list.add(rule);

        rule = new Rule();
        rule.setContent("18501567510");
        rule.setType(Rule.TYPE_STRING);
        rule.setCall(1);
        rule.setSms(1);
        rule.setException(0);
        rule.setRemark("Who knows you?");
        rule.setCreated(now);
        list.add(rule);


        return list;
    }



}
