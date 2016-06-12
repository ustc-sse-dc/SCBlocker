package edu.ustc.sse.scblocker.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import edu.ustc.sse.scblocker.R;
import edu.ustc.sse.scblocker.util.SettingsHelper;

/**
 * Created by dc on 000013/6/13.
 */
public class SettingFragment extends Fragment implements CompoundButton.OnCheckedChangeListener{

    private SettingsHelper mSettingsHelper;

    private Switch sw_sms_enable;
    private Switch sw_call_enable;
    private Switch sw_show_notification;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingsHelper = new SettingsHelper(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        Switch sw_enable = (Switch) view.findViewById(R.id.sw_enable);
        sw_enable.setChecked(mSettingsHelper.isEnable());
        sw_enable.setOnCheckedChangeListener(this);

        sw_sms_enable = (Switch) view.findViewById(R.id.sw_sms_enable);
        sw_sms_enable.setEnabled(sw_enable.isChecked());
        sw_sms_enable.setChecked(mSettingsHelper.isEnableSMS());
        sw_sms_enable.setOnCheckedChangeListener(this);

        sw_call_enable = (Switch) view.findViewById(R.id.sw_call_enable);
        sw_call_enable.setEnabled(sw_enable.isChecked());
        sw_call_enable.setChecked(mSettingsHelper.isEnableCall());
        sw_call_enable.setOnCheckedChangeListener(this);

        sw_show_notification = (Switch) view.findViewById(R.id.sw_show_notification);
        sw_show_notification.setEnabled(sw_enable.isChecked());
        sw_show_notification.setChecked(mSettingsHelper.isShowBlockNotification());
        sw_show_notification.setOnCheckedChangeListener(this);



        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton v, boolean b) {
        switch (v.getId()) {
            case R.id.sw_enable:
                mSettingsHelper.setEnable(b);
                sw_sms_enable.setEnabled(b);
                sw_call_enable.setEnabled(b);
                sw_show_notification.setEnabled(b);
                break;
            case R.id.sw_sms_enable:
                mSettingsHelper.setEnableSMS(b);
                break;
            case R.id.sw_call_enable:
                mSettingsHelper.setEnableCall(b);
                break;
            case R.id.sw_show_notification:
                mSettingsHelper.setShowBlockNotification(b);
                break;
        }
    }
}
