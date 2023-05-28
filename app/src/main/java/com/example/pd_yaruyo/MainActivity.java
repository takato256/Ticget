package com.example.pd_yaruyo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button fetchButton;
    private EditText inputEditText;
    private TextView resultTextView;

    private List<String> keywords;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchButton = findViewById(R.id.fetchButton);
        inputEditText = findViewById(R.id.inputEditText);
        resultTextView = findViewById(R.id.resultTextView);

        keywords = new ArrayList<>();
        handler = new Handler();

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = inputEditText.getText().toString().trim();
                if (!keyword.isEmpty()) {
                    keywords.add(keyword);
                    inputEditText.setText("");
                    updateResult();
                }
            }
        });

        // 定期的に結果を更新する
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

    private class WebScraperTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> results = new ArrayList<>();
            try {
                // WebページのURLを指定してHTMLを取得
                Document doc = Jsoup.connect("https://twitter.com/home").get();

                // pタグを取得
                Elements paragraphs = doc.select("p");

                // キーワードを含むpタグを検索
                for (String keyword : keywords) {
                    for (Element paragraph : paragraphs) {
                        String paragraphText = paragraph.text();
                        if (paragraphText.contains(keyword)) {
                            results.add(paragraphText);
                        }
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
                    sb.append(result).append("\n");
                }
            } else {
                sb.append("No matching paragraphs found");
            }
            resultTextView.setText(sb.toString());
        }
    }
}
