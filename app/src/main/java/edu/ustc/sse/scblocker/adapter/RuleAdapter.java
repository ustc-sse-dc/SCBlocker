package edu.ustc.sse.scblocker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
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
        Rule rule = getItem(position);
        if (rule != null){
            ImageView iv_blockType = holder.getView(R.id.iv_blockType);
            if (rule.getException() == 1){
                iv_blockType.setImageResource(R.drawable.ic_except);
            }else if (rule.getSms() == 1 && rule.getCall() == 1){
                iv_blockType.setImageResource(R.drawable.ic_block_both);
            }else if (rule.getSms() == 1){
                iv_blockType.setImageResource(R.drawable.ic_block_sms);
            }else if (rule.getCall() == 1){
                iv_blockType.setImageResource(R.drawable.ic_block_call);
            }

            TextView tv_ruleContent = holder.getView(R.id.tv_ruleContent);
            tv_ruleContent.setText(rule.getContent());
            TextView tv_remark = holder.getView(R.id.tv_remark);
            tv_remark.setText(rule.getRemark());
        }
    }

    public Rule getItem(int position){
        return rules.get(position);
    }

    public void add(int index, Rule rule){
        rules.add(index, rule);
        notifyDataSetChanged();
    }
    public void replace(int index, Rule rule){
        rules.set(index, rule);
        notifyDataSetChanged();
    }
    public void delete(int index){
        rules.remove(index);
        notifyDataSetChanged();
    }

    /**
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

     **/

    public static class RuleHolder extends RecyclerView.ViewHolder {
        private SparseArray<View> views = new SparseArray<>();
        private View convertView;


        public RuleHolder(View itemView){
            super(itemView);
            convertView = itemView;
        }

        public <T extends View> T getView(int resId){
            View v = views.get(resId);
            if (v == null){
                v = convertView.findViewById(resId);
                views.put(resId, v);
            }
            return (T) v;
        }

    }

}
