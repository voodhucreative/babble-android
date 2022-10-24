package com.dfsl.mybabble;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class EntryTextSpacer {
    public static TextWatcher Watcher(EditText targ){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(s.length() > 0)
                {
                    targ.setLetterSpacing(0.2f);
                }
                else
                {
                    targ.setLetterSpacing(0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }
}
