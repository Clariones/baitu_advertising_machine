package template.tera_pico.com.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        WebView mWebView = (WebView) findViewById(R.id.webView);

        mWebView.loadUrl("http://10.0.2.2:8080/naf/advertiseContentPageManager/preview/ACP000410/");

    }
}
