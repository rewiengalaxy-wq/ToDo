package nl.todo.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * ToDo — offline agenda/takenlijst.
 * De interface staat in assets/www/index.html; deze activity levert
 * lokale opslag (bestand todo.json in de app-map) en het Android-deelmenu.
 */
public class MainActivity extends Activity {

    private static final String TAG = "ToDo";
    private static final String DATA_FILE = "todo.json";

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#F5F6F1"));
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowContentAccess(false);
        s.setAllowFileAccess(false); // assets blijven bereikbaar via file:///android_asset
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setMediaPlaybackRequiresUserGesture(true);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new Bridge(), "AndroidBridge");

        setContentView(webView);

        webView.loadUrl("file:///android_asset/www/index.html");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
            // Datum "vandaag" opnieuw bepalen (bijv. na middernacht of na een reis)
            webView.evaluateJavascript("window.onAppResume && window.onAppResume();", null);
        }
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (webView == null) {
            super.onBackPressed();
            return;
        }
        // Eerst open venster in de app sluiten; anders de app verlaten.
        webView.evaluateJavascript("(window.handleBack && window.handleBack()) ? 'true' : 'false'", value -> {
            if (value == null || !value.contains("true")) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.removeJavascriptInterface("AndroidBridge");
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    /** Brug tussen de interface (JavaScript) en Android. */
    private class Bridge {

        @JavascriptInterface
        public String load() {
            File f = new File(getFilesDir(), DATA_FILE);
            if (!f.exists()) return "";
            try (InputStream in = new FileInputStream(f)) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                return new String(out.toByteArray(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                Log.e(TAG, "Laden mislukt", e);
                return "";
            }
        }

        @JavascriptInterface
        public boolean save(String json) {
            if (json == null) return false;
            File dir = getFilesDir();
            File tmp = new File(dir, DATA_FILE + ".tmp");
            File dst = new File(dir, DATA_FILE);
            try (FileOutputStream out = new FileOutputStream(tmp)) {
                out.write(json.getBytes(StandardCharsets.UTF_8));
                out.getFD().sync();
            } catch (Exception e) {
                Log.e(TAG, "Opslaan mislukt", e);
                return false;
            }
            // Atomair vervangen, zodat er nooit een half bestand achterblijft
            if (!tmp.renameTo(dst)) {
                dst.delete();
                return tmp.renameTo(dst);
            }
            return true;
        }

        @JavascriptInterface
        public void share(final String text) {
            runOnUiThread(() -> {
                try {
                    Intent send = new Intent(Intent.ACTION_SEND);
                    send.setType("text/plain");
                    send.putExtra(Intent.EXTRA_TEXT, text);
                    startActivity(Intent.createChooser(send, "Delen via"));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Delen is niet gelukt", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
