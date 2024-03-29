package ua.nure.mydictionary.UI.Fragments.Parser;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import ua.nure.mydictionary.AppLogic.BrutTextParser;
import ua.nure.mydictionary.R;
import ua.nure.mydictionary.UI.CommonClasses.OpenFileDialog;
import ua.nure.mydictionary.UI.CommonClasses.ToolbarHandler;
import ua.nure.mydictionary.UI.CommonInterfaces.Identifier;

public class ParserFragment extends Fragment implements Identifier, View.OnClickListener {
    public static final String NAME = "ParserFragment";
    private Toolbar mToolbar;
    private Button mButtonOpenFile;
    private Button mButtonParse;
    private ListView mListView;
    private TextView mFileNameTextView;
    private TextView mFileSizeTextView;
    private MyListAdapter mAdapter;
    private String mFilePath;
    private ArrayList<String> mParsedWords = new ArrayList<>();

    public ParserFragment() {
        // Required empty public constructor
    }

    public static ParserFragment newInstance() {
        ParserFragment fragment = new ParserFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_parser, container, false);
        mFileNameTextView = (TextView) rootView.findViewById(R.id.parser_file_name_text_view);
        mFileSizeTextView = (TextView) rootView.findViewById(R.id.parser_file_size_text_view);
        mListView = (ListView) rootView.findViewById(R.id.parser_list_view);
        mAdapter = new MyListAdapter(mParsedWords);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Snackbar.make(view, mParsedWords.get(position), Snackbar.LENGTH_SHORT).show();
            }
        });
        mToolbar = ToolbarHandler.getToolbar(getActivity());
        ToolbarHandler.setOnlyTitleMode(mToolbar);
        ToolbarHandler.getTitleTextView(mToolbar).setText(getString(R.string.title_parser_fragment));
        mButtonOpenFile = (Button) rootView.findViewById(R.id.parser_open_file_button);
        mButtonParse = (Button) rootView.findViewById(R.id.parser_parse_button);
        mButtonOpenFile.setOnClickListener(this);
        mButtonParse.setOnClickListener(this);
        return rootView;
    }

    @Override
    public String getIdentifier() {
        return NAME;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.parser_open_file_button:
                OpenFileDialog dialog = new OpenFileDialog(getActivity());
                dialog.setOpenDialogListener(new OpenFileDialog.OpenDialogListener() {
                    @Override
                    public void OnSelectedFile(String fileName) {
                        mFilePath = fileName;
                        if (mFilePath != null) {
                            File file = new File(mFilePath);
                            mFileNameTextView.setText(file.getName());
                            mFileSizeTextView.setText(file.length() / 1024 + "");
                        }
                    }
                });
                dialog.show();
                break;
            case R.id.parser_parse_button:
                startParsing();
                break;
        }
    }

    private void startParsing() {
        if (mFilePath != null) {
            final BrutTextParser parser = new BrutTextParser();
            final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.dialog_indeterminate)
                    .content(R.string.dialog_please_wait)
                    .progress(true, 0).build();
            dialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    parser.parse(new File(mFilePath));
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            showWords();
                        }
                    });
                    //TODO: need to save method
                    Set<ua.nure.mydictionary.AppLogic.Word> wordSet = parser.getWordSet();
                    for (ua.nure.mydictionary.AppLogic.Word word : wordSet) {
                        mParsedWords.add(word.getWord());
                    }
                }
            }).start();
        } else new MaterialDialog.Builder(getActivity()).title("Error")
                .content("File wasn't chosen").cancelable(true)
                .negativeText(getActivity().getResources()
                        .getString(R.string.std_cancel)).callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.cancel();
                    }
                }).show();
    }

    private void showWords() {
        mAdapter.notifyDataSetChanged();
    }

    private class MyListAdapter extends BaseAdapter {
        private ArrayList<String> mData = new ArrayList<>();

        public MyListAdapter(ArrayList<String> data) {
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.parser_item, parent, false);
            TextView wordTextView = (TextView) view.findViewById(R.id.parser_new_word_text_view);
            wordTextView.setText(mData.get(position));
            return view;
        }
    }
}
