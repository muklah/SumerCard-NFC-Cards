package com.example.myapplication;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NFCReadFragment extends DialogFragment {

    public static final String TAG = NFCReadFragment.class.getSimpleName();
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;

    public static NFCReadFragment newInstance() {

        return new NFCReadFragment();
    }

    private ImageView mReadNfcIcon;
    private TextView mReadCardText;
    private TextView mCurrentBalance;
    private TextView mCurrentBalanceIQD;
    private View mLine1;
    private TextView mPaid;
    private LinearLayout mPaidIQD;
    private View mLine2;
    private Listener mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_read,container,false);

        Toolbar toolbar = view.findViewById(R.id.toolbarId);
        toolbar.setNavigationOnClickListener(view1 -> cancelUpload());

        initViews(view);
        return view;
    }

    private void cancelUpload() {

        Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivityForResult(intent, 0);
    }

    private void initViews(View view) {

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mReadNfcIcon = view.findViewById(R.id.read_nfc_icon);
        mReadCardText = view.findViewById(R.id.read_card_text);
        mCurrentBalance = view.findViewById(R.id.current_balance);
        mCurrentBalanceIQD = view.findViewById(R.id.current_balance_iqd);
        mLine1 = view.findViewById(R.id.line1);
        mPaid = view.findViewById(R.id.paid);
        mPaidIQD = view.findViewById(R.id.paid_iqd);
        mLine2 = view.findViewById(R.id.line2);

        mReadNfcIcon.setVisibility(View.GONE);
        mCurrentBalance.setVisibility(View.GONE);
        mCurrentBalanceIQD.setVisibility(View.GONE);
        mLine1.setVisibility(View.GONE);
        mPaid.setVisibility(View.GONE);
        mPaidIQD.setVisibility(View.GONE);
        mLine2.setVisibility(View.GONE);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (MainActivity)context;
        mListener.onDialogDisplayed();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onDialogDismissed();
    }

    public void onNfcDetected(Ndef ndef){

        readFromNFC(ndef);
    }

    private void readFromNFC(Ndef ndef) {

        try {
            ndef.connect();
            NdefMessage ndefMessage = ndef.getNdefMessage();
            String customer_id = new String(ndefMessage.getRecords()[0].getPayload());
            Log.d(TAG, "readFromNFC: "+customer_id);

            String customer_balance = "50000";
            String paid = "500";
            mUserId = mFirebaseUser.getUid();
            Customer customer = new Customer(customer_id, customer_balance);

            mDatabase.child("customers").child(customer_id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String customerBalance = dataSnapshot.child("customerBalance").getValue(String.class);

                        int newCustomerBalance = 0;

                        try {
                            newCustomerBalance = Integer.parseInt(customerBalance);
                        } catch(NumberFormatException nfe) {
                        }

                        if(newCustomerBalance == 0){
                            mDatabase.child("customers").child(customer_id).child("customerBalance").setValue(customer_balance);
                        }

                        Item item = new Item(customer_id, paid);
                        mDatabase.child("users").child(mUserId).child("payments").push().setValue(item);

                        newCustomerBalance = newCustomerBalance - 500;
                        String sNewCustomerBalance = String.valueOf(newCustomerBalance);

                        mReadCardText.setText("Payment Successful");

                        mReadNfcIcon.setVisibility(View.VISIBLE);
                        mCurrentBalance.setVisibility(View.VISIBLE);
                        mCurrentBalance.setText("Current Balance");
                        mCurrentBalanceIQD.setVisibility(View.VISIBLE);
                        mCurrentBalanceIQD.setText(String.valueOf(sNewCustomerBalance + " IQD"));
                        mLine1.setVisibility(View.VISIBLE);
                        mPaid.setVisibility(View.VISIBLE);
                        mPaid.setText("Paid");
                        mPaidIQD.setVisibility(View.VISIBLE);
                        mLine2.setVisibility(View.VISIBLE);

                        mDatabase.child("customers").child(customer_id).child("customerBalance").setValue(sNewCustomerBalance);

                        for (DataSnapshot issue : dataSnapshot.getChildren()) {
                            // do something with the individual "issues"
                        }
                    }
                    else if(!dataSnapshot.exists()) {
                        mDatabase.child("customers").child(customer_id).setValue(customer);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            ndef.close();

        } catch (IOException | FormatException e) {
            e.printStackTrace();

        }
    }
}
