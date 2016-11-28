package com.tinyappsdev.forestsupply.ui.BaseUI;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tinyappsdev.forestsupply.R;


public class SimpleCustomDialog<AI extends ActivityInterface> extends BaseDialog<AI> {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        LayoutInflater inflater = activity.getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        Bundle bundle = getArguments();

        builder.setTitle(bundle.getString("title"))
                .setMessage(bundle.getString("message"))
                .setPositiveButton(
                        getString(R.string.confirm),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                onConfirm();
                            }
                        }
                )
                .setNegativeButton(
                        getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                onCancel();
                            }
                        }
                )
                .setView(onCreateCustomView(savedInstanceState, builder, inflater, null));

        return  builder.create();
    }

    public View onCreateCustomView(Bundle savedInstanceState,
                                   AlertDialog.Builder builder,
                                   LayoutInflater inflater,
                                   ViewGroup parent)
    {
        return null;
    }

    public void onConfirm() { dismiss(); }
    public void onCancel() { dismiss(); }
}
