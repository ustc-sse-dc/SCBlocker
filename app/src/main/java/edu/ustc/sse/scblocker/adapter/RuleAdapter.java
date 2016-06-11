package edu.ustc.sse.scblocker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.ustc.sse.scblocker.R;
import edu.ustc.sse.scblocker.model.Rule;

/**
 * Created by dc on 000012/6/12.
 */
public class RuleAdapter extends RecyclerView.Adapter<RuleAdapter.RuleHolder> {

    private Context mContext;
    private List<Rule> rules;

    public RuleAdapter(Context context, List<Rule> rules){
        this.mContext = context;
        this.rules = rules == null ? new ArrayList<Rule>() : rules;
    }

    @Override
    public int getItemCount() {
        return rules.size();
    }

    @Override
    public RuleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_rule, parent, false);
        return new RuleHolder(view);
    }

    @Override
    public void onBindViewHolder(RuleHolder holder, int position) {
        Rule rule = rules.get(position);
        if (rule != null){
            holder.bindRule(rule);
        }
    }

    public static class RuleHolder extends RecyclerView.ViewHolder {

        private Rule mRule;

        private ImageView iv_blockType;
        private TextView tv_ruleContent;
        private TextView tv_remark;

        public RuleHolder(View itemView){
            super(itemView);
            iv_blockType = (ImageView)itemView.findViewById(R.id.iv_blockType);
            tv_ruleContent = (TextView)itemView.findViewById(R.id.tv_ruleContent);
            tv_remark = (TextView)itemView.findViewById(R.id.tv_remark);
        }

        public void bindRule(Rule rule){
            mRule = rule;
            if (mRule.getException() == 1){ // 白名单
                iv_blockType.setImageResource(R.drawable.ic_except);
            }else if (mRule.getSms() == 1 && mRule.getCall() == 1){ // block both
                iv_blockType.setImageResource(R.drawable.ic_block_both);
            }else if (mRule.getSms() == 1){ // block sms
                iv_blockType.setImageResource(R.drawable.ic_block_sms);
            }else if (mRule.getCall() == 1){ // block call
                iv_blockType.setImageResource(R.drawable.ic_block_call);
            }

            tv_ruleContent.setText(mRule.getContent());
            tv_remark.setText(mRule.getRemark());

        }

    }

}
