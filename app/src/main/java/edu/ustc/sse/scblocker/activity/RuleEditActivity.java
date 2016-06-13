package edu.ustc.sse.scblocker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.Date;

import edu.ustc.sse.scblocker.R;
import edu.ustc.sse.scblocker.model.Rule;
import edu.ustc.sse.scblocker.util.BlockManager;

/**
 * Created by dc on 000012/6/12.
 */
public class RuleEditActivity extends AppCompatActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener,AdapterView.OnItemSelectedListener{

    public static final String EXTRA_OPERATION = "operation";
    public static final String OPERATION_ADD = "add";
    public static final String OPERATION_MODIFY = "modify";


    private LinearLayout ll_container;

    private EditText et_rule;
    private Spinner sp_type;
    private CheckBox cb_except;
    private Spinner sp_block;
    private EditText et_remark;
    private Button btn_accept;


    private BlockManager mBlockManager;

    private long id = -1;
    private int position = -1;

    public static Intent newIntent(Context context, String operation){
        Intent intent = new Intent(context, RuleEditActivity.class);
        intent.putExtra(EXTRA_OPERATION, operation);
        return intent;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule);

        mBlockManager = new BlockManager(this);

        ll_container = (LinearLayout)findViewById(R.id.ll_container_rule);

        et_rule = (EditText) findViewById(R.id.et_rule);

        sp_type = (Spinner)findViewById(R.id.sp_type);
        sp_type.setOnItemSelectedListener(this);

        cb_except = (CheckBox)findViewById(R.id.cb_except);
        cb_except.setOnCheckedChangeListener(this);

        sp_block = (Spinner)findViewById(R.id.sp_block);
        sp_block.setOnItemSelectedListener(this);

        et_remark = (EditText)findViewById(R.id.et_remark);

        btn_accept = (Button)findViewById(R.id.btn_accept);
        btn_accept.setOnClickListener(this);

        Intent intent = getIntent();
        String operation = intent.getStringExtra(EXTRA_OPERATION);

        if (operation.equals(OPERATION_ADD)){

        }else if (operation.equals(OPERATION_MODIFY)){
            Rule rule = (Rule)intent.getSerializableExtra("rule");
            this.position = intent.getIntExtra("position", -1);
            if (rule != null){
                this.id = rule.getId();
                et_rule.setText(rule.getContent());
                sp_type.setSelection(rule.getType());
                cb_except.setChecked(rule.getException() == 1);
                sp_block.setEnabled(rule.getException() != 1);
                sp_block.setSelection(rule.getSms() == 1 ? rule.getCall() == 1 ? 0 : 1 : 2);
                et_remark.setText(rule.getRemark());

            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.sp_type:
                if (position == Rule.TYPE_KEYWORD){
                    sp_block.setSelection(Rule.BLOCK_SMS);
                }
                break;
            case R.id.sp_block:
                if (position != Rule.BLOCK_SMS && sp_type.getSelectedItemPosition() == Rule.TYPE_KEYWORD){
                    sp_block.setSelection(Rule.BLOCK_SMS);

                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()){
            case R.id.cb_except:
                sp_block.setEnabled(!b); // don't block when sp_block is checked
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_accept:
                // save new/modified rule to database and notify change
                if (TextUtils.isEmpty(et_rule.getText())){
                    et_rule.requestFocus();
                    Snackbar.make(ll_container, R.string.rule_empty_tip, Snackbar.LENGTH_LONG).show();
                }else{
                    Rule rule = new Rule();
                    rule.setContent(et_rule.getText().toString().trim());
                    rule.setType(sp_type.getSelectedItemPosition());
                    rule.setSms(sp_block.isEnabled() && sp_block.getSelectedItemPosition() != Rule.BLOCK_CALL ? 1 : 0);
                    rule.setCall(sp_block.isEnabled() && sp_block.getSelectedItemPosition() != Rule.BLOCK_SMS ? 1 : 0);
                    rule.setException(cb_except.isChecked() ? 1 : 0);
                    rule.setRemark(et_remark.getText().toString().trim());
                    rule.setCreated(new Date().getTime());

                    //insert new/updated rule into database
                    boolean isModify = id != -1;
                    if (isModify){
                        rule.setId(id);
                        mBlockManager.updateRule(rule);
                    } else {
                        rule.setId(mBlockManager.saveRule(rule));
                    }

                    // notify data changed
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", position);
                    bundle.putSerializable("rule", rule);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);

                    finish();
                }

                break;
        }
    }
}
