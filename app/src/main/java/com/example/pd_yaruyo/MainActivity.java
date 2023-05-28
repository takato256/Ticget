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
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Button fetchButton;
    private Button deleteButton;
    private EditText inputEditText;
    private TextView resultTextView;
    private Spinner spinner;

    private List<String> keywords;
    private Handler handler;
    private Runnable runnable;
    private ArrayAdapter<String> adapter;
    private HashMap<String, List<String>> keywordResults;

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
        keywordResults = new HashMap<>();
        handler = new Handler();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, keywords);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String keyword = parent.getItemAtPosition(position).toString();
                List<String> results = keywordResults.get(keyword);
                StringBuilder sb = new StringBuilder();
                if (results != null && !results.isEmpty()) {
                    for (String result : results) {
                        sb.append(result).append("\n");
                    }
                } else {
                    sb.append("No matching paragraphs found");
                }
                resultTextView.setText(sb.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                resultTextView.setText("");
            }
        });

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = inputEditText.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    keywords.add(keyword);
                    inputEditText.setText("");
                    adapter.notifyDataSetChanged();
                    updateResult();
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = spinner.getSelectedItemPosition();
                if (!keywords.isEmpty()) {
                    String keyword = keywords.remove(position);
                    keywordResults.remove(keyword);
                    adapter.notifyDataSetChanged();
                    resultTextView.setText("");
                }
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                updateResult();
                handler.postDelayed(this, 3600000); // 1時間ごとに更新
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 5000); // 5秒後から定期実行を開始
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); // 定期実行を停止
    }

    private void updateResult() {
        new WebScraperTask().execute();
    }

    private class WebScraperTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // WebページのURLを指定してHTMLを取得
                Document doc = Jsoup.connect("https://takato256.github.io/PacketStreet/").get();

                // pタグを取得
                Elements paragraphs = doc.select("p");

                // キーワードを含むpタグを検索
                for (String keyword : keywords) {
                    List<String> results = new ArrayList<>();
                    for (Element paragraph : paragraphs) {
                        String paragraphText = paragraph.text();
                        if (paragraphText.contains(keyword)) {
                            results.add(paragraphText);
                        }
                    }
                    keywordResults.put(keyword, results);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter.notifyDataSetChanged();
        }
    }
}
