package com.lyc.callerinfo.index.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lyc.callerinfo.R;
import com.lyc.callerinfo.contract.MainContract;
import com.lyc.callerinfo.model.db.InCallBean;

import java.util.ArrayList;
import java.util.List;

public class CallerAdapter extends RecyclerView.Adapter<CallerAdapter.ViewHolder> {
    private static final String TAG = CallerAdapter.class.getSimpleName();
    private Context mContext;
    private List<InCallBean> mList;
    private MainContract.Presenter mPresenter;

    public CallerAdapter(Context context, MainContract.Presenter presenter) {
        this.mContext = context;
        mPresenter = presenter;
        mList = new ArrayList<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.card_item, viewGroup, false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder holder, int i) {
        InCallBean model = mList.get(i);
        holder.bind(model);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public void replaceData(List<InCallBean> models) {
        mList = models;
        notifyDataSetChanged();
    }

    public InCallBean getItem(int position) {
        return mList.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        final CardView cardView;
        final TextView text;
        final TextView number;
        final LinearLayout detail;
        final TextView time;
        final TextView ringTime;
        final TextView duration;
        InCallBean model;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            text = (TextView) itemView.findViewById(R.id.text);
            number = (TextView) itemView.findViewById(R.id.number);
            detail = (LinearLayout) itemView.findViewById(R.id.detail);
            time = (TextView) itemView.findViewById(R.id.time);
            ringTime = (TextView) itemView.findViewById(R.id.ring_time);
            duration = (TextView) itemView.findViewById(R.id.duration);

            cardView.setOnClickListener(this);
            cardView.setOnLongClickListener(this);
        }

        public void setAlpha(float alpha) {
            cardView.setAlpha(alpha);
        }

        public void bind(InCallBean model) {
            this.model = model;
            if (model.getIszhapian().equals("1")) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.red_light));
                text.setText(model.getProvince() + " " + model.getCity() + " 已被" + model.getRpt_cnt() + "人标记为" + model.getRpt_type());
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.blue_light));
                text.setText(model.getProvince() + " " + model.getCity() + " " + model.getSp());
            }
            number.setText(model.getPhone());
        }

        @Override
        public void onClick(View v) {

        }

        @Override
        public boolean onLongClick(View v) {
            mPresenter.itemOnLongClicked(model);
            return false;
        }
    }
}
