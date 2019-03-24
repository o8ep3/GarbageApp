package com.example.garbagedayandroidapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class EditFragment extends Fragment {

    //タイトル変更
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().setTitle(" ごみ収集日の編集");

    }

    private static ArrayAdapter<String> adapter = null;
    private ListView listView = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit, container, false);

        //fragment_editのListViewのリソースID
        listView = (ListView) v.findViewById(R.id.myListView);
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        //要素を長押しすると削除
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ListView list = (ListView) parent;
                String deleteItem = (String) list.getItemAtPosition(position);
                adapter.remove(deleteItem);
                Toast.makeText(getActivity(), "削除しました", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        //fabボタンを押すとアラートを表示し，ごみ収集日の予定を入力
        FloatingActionButton floatingActionButton = v.findViewById(R.id.fab_add);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ダイアログを表示する
                DialogFragment newFragment = new GarbageDialogFragment();
                newFragment.show(getFragmentManager(), "GarbageSetting");

            }
        });
        return v;
    }

    public static class GarbageDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
            final View content = inflater.inflate(R.layout.dialog, null);

            //タイミングのspinnerを取得
            Spinner spinnerGarbageTiming = (Spinner)content.findViewById(R.id.spinner_garbage_timing);
            //選択肢を配列, adapterに格納
            String arrayGarbageDay[] = getResources().getStringArray(R.array.Entries_garbage_day);
            String arrayGarbageDaySpecific[] = getResources().getStringArray(R.array.Entries_garbage_day_specific);
            final ArrayAdapter<String> adapterGarbageDay =
                    new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arrayGarbageDay);
            final ArrayAdapter<String> adapterGarbageDaySpecific =
                    new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arrayGarbageDaySpecific);
            //タイミングの選ばれ方によって選択肢を変える
            spinnerGarbageTiming.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Spinner spinner = (Spinner) parent;
                    Spinner spinnerGarbageDay = (Spinner) content.findViewById(R.id.spinner_garbage_day);
                    if (spinner.getSelectedItem().toString().equals("毎週")) {
                        adapterGarbageDay.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerGarbageDay.setAdapter(adapterGarbageDay);
                    } else {
                        adapterGarbageDaySpecific.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerGarbageDay.setAdapter(adapterGarbageDaySpecific);
                    }
                }

                @Override
                //何も選ばれていない時
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            //アラートダイアログを生成
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(content);
            builder.setTitle("ごみ収集日を追加");

            builder.setPositiveButton("追加", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //オーケーボタンクリック処理
                    Spinner spinnerGarbageType = (Spinner) content.findViewById(R.id.spinner_garbage_type);
                    Spinner spinnerGarbageTiming = (Spinner) content.findViewById(R.id.spinner_garbage_timing);
                    Spinner spinnerGarbageDay = (Spinner) content.findViewById(R.id.spinner_garbage_day);
                    adapter.add(spinnerGarbageType.getSelectedItem().toString() + " " + spinnerGarbageTiming.getSelectedItem().toString() + "   " + spinnerGarbageDay.getSelectedItem().toString());
                }
            });
            builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //キャンセルボタンクリック処理
                }
            });
            //表示
            return builder.create();
        }
    }


}
