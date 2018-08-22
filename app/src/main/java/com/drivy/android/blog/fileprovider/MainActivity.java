package com.drivy.android.blog.fileprovider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

    private static final String SHARED_PROVIDER_AUTHORITY = "com.drivy.android.blog.fileprovider";
    private static final String SHARED_FOLDER = "shared";

    private static void writeBitmap(final File destination,
            final Bitmap bitmap) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(destination);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            close(outputStream);
        }
    }

    private static void close(final Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        copyAssets();

/*        WebView webView = new WebView(MainActivity.this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setWebViewClient(new Callback());
        String pdfURL = "http://www.pdf995.com/samples/pdf.pdf";
        String pdfURL1 = "http://docs.google.com/gview?embedded=true&url=" + pdfURL;
        webView.loadUrl(pdfURL);
        setContentView(webView);*/
    }

    @Nullable
    private File getPrimaryAppFilesDir(@NonNull Context context) {

        File dir = context.getFilesDir();
        if (dir != null) {
            return dir.getAbsoluteFile();
        }

        return null;
    }

    public void share(final View view) throws IOException {


        // Create a random image and save it in private app folder
        final File sharedFile = new File(this.getFilesDir(), "sample.pdf");

        // Get the shared file's Uri
        final Uri uri = FileProvider.getUriForFile(this, SHARED_PROVIDER_AUTHORITY, sharedFile);

        // Create a intent
        final ShareCompat.IntentBuilder intentBuilder = ShareCompat.IntentBuilder.from(this)
                .setType("image/*")
                .addStream(uri);

        // create new Intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // set flag to give temporary permission to external app to use your FileProvider
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // generate URI, I defined authority as the application ID in the Manifest, the last
        // param is file I want to open
        // I am opening a PDF file so I give it a valid MIME type
        intent.setDataAndType(uri, "application/pdf");

        // validate that the device can open your File!
        PackageManager pm = getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            startActivity(intent);
        }
    }

    @NonNull
    private File createFile() throws IOException {
        final RandomBitmapFactory bitmapFactory = new RandomBitmapFactory();
        final Bitmap randomBitmap = bitmapFactory.createRandomBitmap();

        final File sharedFolder = new File(getFilesDir(), SHARED_FOLDER);
        sharedFolder.mkdirs();

        final File sharedFile = File.createTempFile("picture", ".png", sharedFolder);
        sharedFile.createNewFile();

        writeBitmap(sharedFile, randomBitmap);
        return sharedFile;
    }

    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(getPrimaryAppFilesDir(this), filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch (IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private class Callback extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(
                WebView view, String url) {
            return (false);
        }
    }
}
