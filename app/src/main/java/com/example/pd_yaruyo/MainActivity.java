package com.example.pd_yaruyo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button fetchButton;
    private Button deleteButton;
    private EditText inputEditText;
    private TextView resultTextView;
    private Spinner spinner;

    private List<String> keywords;
    private Handler handler;
    private Runnable runnable;
    private String scrapingKeyword = "受付期間";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchButton = findViewById(R.id.fetchButton);
        deleteButton = findViewById(R.id.deleteButton);
        inputEditText = findViewById(R.id.inputEditText);
        resultTextView = findViewById(R.id.resultTextView);
        spinner = findViewById(R.id.spinner);

        keywords = new ArrayList<>();
        handler = new Handler();

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = inputEditText.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    keywords.add(keyword);
                    inputEditText.setText("");
                    updateSpinner();
                    updateResult();
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedKeyword = (String) spinner.getSelectedItem();
                if (selectedKeyword != null) {
                    keywords.remove(selectedKeyword);
                    updateSpinner();
                    updateResult();
                }
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                updateResult();
                handler.postDelayed(this, 3600000); // 1時間ごとに情報を更新
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 5000); // 5秒後に情報を取得
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); // 実行停止
    }

    private void updateResult() {
        for (String keyword : keywords) {
            new WebScraperTask(keyword).execute();
        }
    }

    private void updateSpinner() {
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, keywords);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
    }

    private class WebScraperTask extends AsyncTask<Void, Void, List<String>> {

        private String keyword; // ユーザーが入力するクエリ部分

        public WebScraperTask(String keyword) {
            this.keyword = keyword;
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> results = new ArrayList<>();
            try {
                Document doc = Jsoup.connect("https://eplus.jp/sf/detail/0231370001").get();
                Elements statusElements = doc.select("p");

                for (Element statusElement : statusElements) {
                    String statusText = statusElement.text();
                    if (statusText.contains(scrapingKeyword)) {
                        if (statusText.length() > 5) {
                            statusText = statusText.substring(5);  // 最初の5文字をスキップ
                        }
                        results.add(statusText);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<String> results) {
            StringBuilder sb = new StringBuilder();
            if (!results.isEmpty()) {
                for (String result : results) {
                    sb.append("- ").append(result).append("\n");
                }
            } else {
                sb.append("No matching status found");
            }
            resultTextView.setText(sb.toString());
        }
    }
}
