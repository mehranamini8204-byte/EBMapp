package com.example.ebmsummary;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView web;
    private WebView printer; // keep a reference so it is not garbage-collected during printing

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        web.setWebChromeClient(new WebChromeClient());
        web.addJavascriptInterface(new Bridge(), "AndroidPrint");
        web.loadUrl("file:///android_asset/index.html");
    }

    class Bridge {
        @JavascriptInterface public void printHtml(final String html, final String title) {
            runOnUiThread(() -> {
                printer = new WebView(MainActivity.this);
                printer.setWebViewClient(new WebViewClient() {
                    @Override public void onPageFinished(WebView v, String url) {
                        PrintManager pm = (PrintManager) getSystemService(Context.PRINT_SERVICE);
                        pm.print(title, v.createPrintDocumentAdapter(title),
                            new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build());
                    }
                });
                printer.loadDataWithBaseURL("https://localhost/", html, "text/html", "UTF-8", null);
            });
        }
    }

    @Override public void onBackPressed() {
        // In the reader view, go back to the index first; otherwise exit.
        web.evaluateJavascript("(function(){var r=document.getElementById('reader-view');"
            + "if(r&&r.style.display==='block'){showIndex();return 1;}return 0;})()",
            v -> { if ("0".equals(v)) finish(); });
    }
}
