package com.example.pd_yaruyo;
import android.os.AsyncTask;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    private Button fetchButton;
    private EditText inputEditText;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchButton = findViewById(R.id.fetchButton);
        inputEditText = findViewById(R.id.inputEditText);
        resultTextView = findViewById(R.id.resultTextView);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = inputEditText.getText().toString();
                new WebScraperTask(inputText).execute();
            }
        });
    }

    private class WebScraperTask extends AsyncTask<Void, Void, String> {

        private String inputText;

        public WebScraperTask(String inputText) {
            this.inputText = inputText;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // WebページのURLを指定してHTMLを取得
                Document doc = Jsoup.connect("https://twitter.com/home").get();

                // pタグを取得
                Elements paragraphs = doc.select("p");

                // 入力された文字列を含むpタグを検索
                StringBuilder sb = new StringBuilder();
                for (Element paragraph : paragraphs) {
                    String paragraphText = paragraph.text();
                    if (paragraphText.contains(inputText)) {
                        sb.append(paragraphText).append("\n");
                    }
                }

                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null && !result.isEmpty()) {
                resultTextView.setText(result);
            } else {
                resultTextView.setText("No matching paragraphs found");
            }
        }
    }
}
