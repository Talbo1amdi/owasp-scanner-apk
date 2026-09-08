package ir.owasp.scanner;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Base64;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.OutputStream;

@CapacitorPlugin(name = "SaveAs")
public class SaveAsPlugin extends Plugin {

    private static final String SAVE_PDF = "savePdf";

    @PluginMethod
    public void savePdf(PluginCall call) {
        String base64 = call.getString("data");
        String fileName = call.getString("fileName", "scan-report.pdf");

        if (base64 == null) {
            call.reject("data (base64) is required");
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        startActivityForResult(call, intent, SAVE_PDF);
    }

    @ActivityCallback
    private void savePdf(PluginCall call, ActivityResult result) {
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null || result.getData().getData() == null) {
            JSObject ret = new JSObject();
            ret.put("canceled", true);
            call.resolve(ret);
            return;
        }

        try {
            Uri uri = result.getData().getData();
            String base64 = call.getString("data");
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);

            OutputStream os = getContext().getContentResolver().openOutputStream(uri, "w");
            os.write(bytes);
            os.flush();
            os.close();

            String displayName = queryDisplayName(uri);
            String readablePath = readablePath(uri);

            JSObject ret = new JSObject();
            ret.put("canceled", false);
            ret.put("uri", uri.toString());
            ret.put("displayName", displayName);
            ret.put("path", readablePath);
            call.resolve(ret);
        } catch (Exception ex) {
            call.reject("نوشتن پرونده ناموفق بود: " + ex.getMessage(), ex);
        }
    }

    private String queryDisplayName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) {
                    return cursor.getString(idx);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "";
    }

    private String readablePath(Uri uri) {
        try {
            String docId = DocumentsContract.getDocumentId(uri);
            if (docId != null && docId.startsWith("primary:")) {
                String relative = docId.substring("primary:".length());
                return "/storage/emulated/0/" + relative;
            }
        } catch (Exception ignored) {
        }
        return uri.toString();
    }
}