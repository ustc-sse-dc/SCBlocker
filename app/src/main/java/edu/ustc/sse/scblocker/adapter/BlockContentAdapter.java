package edu.ustc.sse.scblocker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.ustc.sse.scblocker.R;
import edu.ustc.sse.scblocker.model.BlockContent;

/**
 * Created by dc on 000011/6/11.
 */
public class BlockContentAdapter extends RecyclerView.Adapter<BlockContentAdapter.ViewHolder> {
    public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private Context mContext;
    public List<BlockContent> data;

    public BlockContentAdapter(Context context, List<BlockContent> data){
        mContext = context;
        this.data = data == null ? new ArrayList<BlockContent>() : data;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.item_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BlockContent content = data.get(position);
        if (content != null){
            holder.bindContent(content);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private BlockContent mContent;
        private TextView tv_number;
        private TextView tv_content;
        private TextView tv_type;
        private TextView tv_created;

        public ViewHolder(View itemView){
            super(itemView);
            tv_number = (TextView)itemView.findViewById(R.id.tv_number);
            tv_content = (TextView)itemView.findViewById(R.id.tv_content);
            tv_type = (TextView)itemView.findViewById(R.id.tv_type);
            tv_created = (TextView)itemView.findViewById(R.id.tv_created);
        }

        public void bindContent(BlockContent content){
            mContent = content;
            tv_number.setText(mContent.getNumber());
            tv_content.setText(mContent.getContent());
            tv_type.setText(mContent.getType() == BlockContent.BLOCK_CALL ? "Call" : "SMS");
            tv_created.setText(format.format(new Date(mContent.getCreated())));
        }


    }


}
