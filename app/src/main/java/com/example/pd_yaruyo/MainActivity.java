package com.example.pd_yaruyo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;


import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button fetchButton;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fetchButton = findViewById(R.id.fetchButton);
        resultTextView = findViewById(R.id.resultTextView);

        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new WebScraperTask().execute();
            }
        });
    }

    private class WebScraperTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                // WebページのURLを指定してHTMLを取得
                Document doc = Jsoup.connect("https://twitter.com/home").get();

                // pタグを取得
                Elements paragraphs = doc.select("p");

                // pタグのテキストを連結して返す
                StringBuilder sb = new StringBuilder();
                for (Element paragraph : paragraphs) {
                    sb.append(paragraph.text()).append("\n");
                }
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                resultTextView.setText(result);
            } else {
                resultTextView.setText("Failed to fetch paragraph text");
            }
        }
    }

}
