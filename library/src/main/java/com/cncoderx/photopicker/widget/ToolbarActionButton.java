package com.cncoderx.photopicker.widget;

import android.content.Context;
import android.support.annotation.StringRes;
import android.support.v4.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.cncoderx.photopicker.R;

/**
 * Created by admin on 2017/2/21.
 */
public class ToolbarActionButton extends ActionProvider {
    private Context context;
    private TextView mTextView;
    private View.OnClickListener mListener;

    public ToolbarActionButton(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public View onCreateActionView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.toolbar_button, null);
        mTextView = (TextView) view.findViewById(R.id.tv_album_name);
        mTextView.setOnClickListener(mListener);
        return view;
    }

    public void setText(@StringRes int resId) {
        mTextView.setText(resId);
    }

    public void setText(CharSequence text) {
        mTextView.setText(text);
    }

    public CharSequence getText() {
        return mTextView.getText();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mListener = listener;
    }

    public void setEnabled(boolean enabled) {
        if (mTextView != null) {
            mTextView.setEnabled(enabled);
        }
    }
}
