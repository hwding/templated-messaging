package com.amastigote.templatemsg.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amastigote.templatemsg.R;
import com.amastigote.templatemsg.module.SMSUtils;
import com.amastigote.templatemsg.module.TemplateUtils;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.core.ContactPickerActivity;
import com.onegravity.contactpicker.group.Group;
import com.onegravity.contactpicker.picture.ContactPictureType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amastigote.templatemsg.module.PermissionUtils.chk_pem;

/*
    todo: there should be a delivery report...
 */

/*
    todo: there will be a 'about' menu item, menu's xml is ready...
 */

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity {
    final private String var_symbol = "#var";
    final private String name_symbol = "#name";
    final private int PICK_CONTACT = 1;
    final private String SSA = "SENT_SMS_ACTION";
    final private String ITP = "INTENT_TARGET_PHONE";

    private ArrayList<EditText> et_arrlst = new ArrayList<>();

    private LinearLayout whole_ll;
    private LinearLayout ll;
    private EditText ed_tplt;
    private Button btn_cfm;
    private Button btn_clr;
    private Button btn_snd;

    private Button btn_ist_var;
    private Button btn_ist_name;

    private LinearLayout sms_pool;

    private String sms_ready_to_send;
    private Map<String, String> sms_ready_to_send_hashMap = new HashMap<>();
    private Object[] key_array;

    private ProgressDialog pd;

    private BroadcastReceiver bdc_rsv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setElevation(0);

        whole_ll = (LinearLayout) findViewById(R.id.whole_ll);
        ll = (LinearLayout) findViewById(R.id.e);
        ed_tplt = (EditText) findViewById(R.id.g);
        btn_cfm = (Button) findViewById(R.id.a);
        btn_clr = (Button) findViewById(R.id.b);
        btn_snd = (Button) findViewById(R.id.c);
        btn_ist_var = (Button) findViewById(R.id.k);
        btn_ist_name = (Button) findViewById(R.id.l);

        set_btn_listener();
        reg_all_rsv();
        chk_pem(MainActivity.this, MainActivity.this);
    }

    private void generate_var_input(int var_num) {
        for (int i = 0; i < var_num; i++) {
            View a = View.inflate(MainActivity.this, R.layout.var_input, null);
            EditText et = (EditText) a.findViewById(R.id.d);
            TextView tv = (TextView) a.findViewById(R.id.f);
            String tx = var_symbol + " " + i;
            tv.setText(tx);
            et_arrlst.add(et);
            ll.addView(a);
        }
    }

    private int set_spot_color(String string, ArrayList<Integer> var_lst, ArrayList<Integer> name_lst) {
        SpannableString ss = new SpannableString(string);
        for (int e : var_lst)
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(MainActivity.this, R.color.colorAccent)), e, e + var_symbol.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        for (int e : name_lst)
            ss.setSpan(new ForegroundColorSpan(ContextCompat.getColor(MainActivity.this, R.color.blue)), e, e + name_symbol.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ed_tplt.setText(ss);
        return var_lst.size();
    }

    private void set_btn_listener() {
        btn_snd.setEnabled(false);
        btn_cfm.setOnClickListener(view -> {
            String s = ed_tplt.getText().toString().trim();
            generate_var_input(set_spot_color(s,
                    TemplateUtils.spot_placeholders(s, var_symbol),
                    TemplateUtils.spot_placeholders(s, name_symbol)));
            ed_tplt.setEnabled(false);
            view.setEnabled(false);
            btn_snd.setEnabled(true);

            btn_ist_name.setEnabled(false);
            btn_ist_var.setEnabled(false);
        });

        btn_clr.setOnClickListener(view -> {
            ed_tplt.setText("");
            ed_tplt.setEnabled(true);
            btn_cfm.setEnabled(true);
            btn_snd.setEnabled(false);
            ll.removeAllViews();
            et_arrlst.clear();

            btn_ist_name.setEnabled(true);
            btn_ist_var.setEnabled(true);
        });

        btn_snd.setOnClickListener(view -> {
            String raw = ed_tplt.getText().toString().trim();
            for (EditText e : et_arrlst)
                raw = raw.replaceFirst(var_symbol, e.getText().toString().trim());
            sms_ready_to_send = raw;
            cfm_sdn_assign_tel();
            chg_all_var_input_et(false, false);
        });

        btn_ist_var.setOnClickListener(view -> {
            int index = ed_tplt.getSelectionStart();
            Editable editable = ed_tplt.getEditableText();
            editable.insert(index, var_symbol);
        });

        btn_ist_name.setOnClickListener(view -> {
            int index = ed_tplt.getSelectionStart();
            Editable editable = ed_tplt.getEditableText();
            editable.insert(index, name_symbol);
        });
    }

    private void chg_all_var_input_et(boolean enabled, boolean clear) {
        for (EditText e : et_arrlst) {
            if (enabled) {
                if (clear)
                    e.setText("");
                e.setEnabled(true);
            } else
                e.setEnabled(false);
        }
    }

    private void cfm_sdn_assign_tel() {
        View v = View.inflate(MainActivity.this, R.layout.cfm_snd_assign_tel, null);
        final EditText et = (EditText) v.findViewById(R.id.i);
        ImageView iv = (ImageView) v.findViewById(R.id.j);
        ImageView iv_b = (ImageView) v.findViewById(R.id.add_phone);
        sms_pool = (LinearLayout) v.findViewById(R.id.pool);

        LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());

        iv.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ContactPickerActivity.class)
                    .putExtra(ContactPickerActivity.EXTRA_THEME, R.style.ContactPicker_Theme_Dark)
                    .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE, ContactPictureType.ROUND.name())
                    .putExtra(ContactPickerActivity.EXTRA_SHOW_CHECK_ALL, true)
                    .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION, ContactDescription.PHONE.name())
                    .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER, ContactSortOrder.AUTOMATIC.name())
                    .putExtra(ContactPickerActivity.EXTRA_ONLY_CONTACTS_WITH_PHONE, true);
            MainActivity.this.startActivityForResult(intent, PICK_CONTACT);
        });

        iv_b.setOnClickListener(view -> {
            String p = et.getText().toString().trim().replaceAll(" ", "");
            if (!"".equals(p)) {
                View v_b = layoutInflater.inflate(R.layout.sms_item, null);

                TextView t_a = (TextView) v_b.findViewById(R.id.si_contact);
                TextView t_b = (TextView) v_b.findViewById(R.id.si_content);

                t_a.setText(p);
                t_b.setText(sms_ready_to_send);

                if (!sms_ready_to_send_hashMap.containsKey(p)) {
                    sms_ready_to_send_hashMap.put(p, t_b.getText().toString());
                    v_b.setTag(p);
                    sms_pool.addView(v_b);
                }
                et.setText(null);
            }
        });

        AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
        adb.setTitle(getString(R.string.cfm_sms))
                .setView(v)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.send), (dialogInterface, i) -> {
                    if (!(sms_ready_to_send_hashMap.size() == 0)) {
                        pd = new ProgressDialog(MainActivity.this);
                        pd.setMessage(getString(R.string.in_prg) + "   1/" + sms_ready_to_send_hashMap.size());
                        pd.setCancelable(false);
                        pd.show();
                        key_array = sms_ready_to_send_hashMap.keySet().toArray();
                        Intent si = new Intent(SSA);
                        si.putExtra(ITP, (String) key_array[0]);
                        System.out.println("send to " + (String) key_array[0]);
                        System.out.println("index is 0");
                        PendingIntent spi = PendingIntent.getBroadcast(MainActivity.this, 0, si, PendingIntent.FLAG_UPDATE_CURRENT);
                        SMSUtils.snd_msg((String) sms_ready_to_send_hashMap.get((String) key_array[0]), (String) key_array[0], spi);
                    } else {
                        Snackbar.make(whole_ll, getString(R.string.empty_list), Snackbar.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(getString(R.string.back), (dInterface, i) -> {
                    sms_ready_to_send_hashMap.clear();
                })
                .show();
    }

    private void send(int index) {
        System.out.println("index is " + index);
        System.out.println("send to " + (String) key_array[index]);
        Intent si = new Intent(SSA);
        si.putExtra(ITP, (String) key_array[index]);
        PendingIntent spi = PendingIntent.getBroadcast(MainActivity.this, 0, si, PendingIntent.FLAG_UPDATE_CURRENT);
        SMSUtils.snd_msg((String) sms_ready_to_send_hashMap.get((String) key_array[index]), (String) key_array[index], spi);
    }

    private void reg_all_rsv() {
        bdc_rsv = new BroadcastReceiver() {
            int target_suc = 0;
            int target_fai = 0;
            boolean is_sending = false;
            int target_sum = 0;
            List<String> failed_key = new ArrayList<>();

            @Override
            public void onReceive(Context context, Intent intent) {
                if (!is_sending) {
                    is_sending = true;
                    target_sum = sms_ready_to_send_hashMap.size();
                }
                System.out.println("result code is " + getResultCode());
                switch (getResultCode()) {
                    case RESULT_OK:
                        target_suc++;
                        sms_ready_to_send_hashMap.remove(intent.getStringExtra(ITP));
                        break;
                    default:
                        if (!failed_key.contains(intent.getStringExtra(ITP)))
                            failed_key.add(intent.getStringExtra(ITP));
                        target_fai++;
                        break;
                }
                if (target_fai + target_suc == target_sum) {
                    is_sending = false;
                    pd.dismiss();
                    target_suc = 0;
                    target_fai = 0;
                    target_sum = 0;

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(getString(R.string.snd_report));
                    builder.setCancelable(false);
                    if (target_fai != 0) {
                        builder.setMessage(target_fai + getString(R.string.fail));
                        builder.setPositiveButton(getString(R.string.resend), (a, b) -> {
                            key_array = failed_key.toArray();
                        });
                        builder.setNeutralButton(getString(R.string.back), (a, b) -> {
                            chg_all_var_input_et(true, false);
                            sms_ready_to_send_hashMap.clear();
                            failed_key.clear();
                        });
                        builder.setNegativeButton(getString(R.string.cancel), (a, b) -> {
                            chg_all_var_input_et(true, true);
                            sms_ready_to_send_hashMap.clear();
                            failed_key.clear();
                        });
                    } else {
                        builder.setMessage(getString(R.string.all_succ));
                        builder.setPositiveButton(getString(R.string.ok), (a, b) -> {
                            chg_all_var_input_et(true, true);
                            sms_ready_to_send_hashMap.clear();
                            failed_key.clear();
                        });
                    }
                    builder.create().show();
                } else {
                    pd.setMessage(getString(R.string.in_prg) + "   " + ((int) target_fai + (int) target_suc + (int) 1) + "/" + key_array.length);
                    send(target_fai + target_suc);
                }
            }
        };
        MainActivity.this.registerReceiver(bdc_rsv, new IntentFilter(SSA));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bdc_rsv);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        chk_pem(MainActivity.this, MainActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)) {

            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());

            List<Contact> contacts = (List<Contact>) data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA);
            for (Contact contact : contacts) {
                View v = layoutInflater.inflate(R.layout.sms_item, null);

                TextView t_a = (TextView) v.findViewById(R.id.si_contact);
                TextView t_b = (TextView) v.findViewById(R.id.si_content);

                String p = contact.getPhone(0).replaceAll(" ", "");
                String n = contact.getFirstName();

                t_a.setText(n);
                t_b.setText(sms_ready_to_send.replaceAll(name_symbol, contact.getFirstName()));

                if (!sms_ready_to_send_hashMap.containsKey(p)) {
                    sms_ready_to_send_hashMap.put(p, t_b.getText().toString());
                    v.setTag(p);
                    sms_pool.addView(v);
                }
            }

            List<Group> groups = (List<Group>) data.getSerializableExtra(ContactPickerActivity.RESULT_GROUP_DATA);
            for (Group group : groups) {
                List<Contact> contacts_from_group = (List<Contact>) group.getContacts();
                for (Contact contact : contacts_from_group) {
                    View v = layoutInflater.inflate(R.layout.sms_item, null);

                    TextView t_a = (TextView) v.findViewById(R.id.si_contact);
                    TextView t_b = (TextView) v.findViewById(R.id.si_content);

                    String p = contact.getPhone(0).replaceAll(" ", "");
                    String n = contact.getFirstName();

                    t_a.setText(n);
                    t_b.setText(sms_ready_to_send.replaceAll(name_symbol, contact.getFirstName()));

                    if (!sms_ready_to_send_hashMap.containsKey(p)) {
                        sms_ready_to_send_hashMap.put(p, t_b.getText().toString());
                        v.setTag(p);
                        sms_pool.addView(v);
                    }
                }
            }
        }
    }
}
