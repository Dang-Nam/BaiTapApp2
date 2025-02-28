package com.example.baiapp2;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private TextView textViewLastId;
    private Button btnCheckData;
    private Handler handler = new Handler();
    private int lastSavedId = -1;
    private static final String CHANNEL_ID = "notify_channel";
    private static final String BASE_URL = "http://192.168.0.107:5000/api"; // Địa chỉ API

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewLastId = findViewById(R.id.textViewLastId);
        btnCheckData = findViewById(R.id.btnCheckData);

        // ✅ Xóa lastSavedId trong SharedPreferences (Chạy 1 lần khi app mở)
        SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
        editor.remove("last_saved_id");
        editor.apply();
        Log.d("DEBUG", "🗑 Xóa last_saved_id trong SharedPreferences!");

        // ✅ Lấy lại lastSavedId (nếu chưa có sẽ là -1)
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        lastSavedId = prefs.getInt("last_saved_id", -1);
        Log.d("DEBUG", "✅ Ứng dụng khởi động, lastSavedId: " + lastSavedId);

        createNotificationChannel(); // Tạo kênh thông báo

        btnCheckData.setOnClickListener(v -> {
            Log.d("DEBUG", "➡ Nút 'Kiểm tra dữ liệu' được ấn!");
            checkData();
        });

        // ✅ Chạy kiểm tra dữ liệu mỗi 30 giây, nhưng KHÔNG kiểm tra ngay khi mở app
        handler.postDelayed(runnable, 30000); // Đợi 30 giây trước khi kiểm tra lần đầu
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            checkData();
            handler.postDelayed(this, 30000); // ✅ Chỉ gọi lại sau 30 giây
        }
    };



    private void checkData() {
        Log.d("DEBUG", "📡 Đang kiểm tra dữ liệu từ API...");

        String url = BASE_URL + "/last_id";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        int newId = response.getInt("last_id");
                        Log.d("DEBUG", "✅ API trả về last_id: " + newId + ", lastSavedId: " + lastSavedId);

                        // ✅ Nếu ID mới hơn, cập nhật & fetch dữ liệu
                        if (lastSavedId == -1 || newId > lastSavedId) {

                            lastSavedId = newId;
                            SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
                            editor.putInt("last_saved_id", lastSavedId);
                            editor.apply();

                            textViewLastId.setText("Last ID: " + newId);
                            fetchData(newId);
                        } else {
                            Log.d("DEBUG", "⚠ Không có ID mới, bỏ qua fetchData()");
                        }
                    } catch (JSONException e) {
                        Log.e("API_ERROR", "❌ Lỗi JSON: " + e.getMessage());
                    }
                },
                error -> Log.e("API_ERROR", "❌ Lỗi khi gọi API: " + error.getMessage())
        );

        queue.add(jsonRequest);
    }

    private void fetchData(int id) {
        Log.d("DEBUG", "📥 Gọi fetchData() với ID: " + id);
        String url = BASE_URL + "/get_id?id=" + id;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray dataArray = response.getJSONArray("data");
                        if (dataArray.length() > 0) {
                            JSONObject dataObject = dataArray.getJSONObject(0);
                            String title = dataObject.optString("title", "Thông báo mới");
                            String body = dataObject.optString("body", "Bạn có dữ liệu mới.");
                            String time = dataObject.optString("time", "");

                            Log.d("DEBUG", "🔔 Nhận dữ liệu từ API: " + title + " - " + body);
                            sendNotification(id, title, body, time);
                            playSound();
                        }
                    } catch (JSONException e) {
                        Log.e("API_ERROR", "❌ Lỗi JSON khi lấy dữ liệu: " + e.getMessage());
                    }
                },
                error -> Log.e("API_ERROR", "❌ Lỗi khi gọi API get_id: " + error.getMessage())
        );

        queue.add(jsonRequest);
    }

    private void sendNotification(int id, String title, String body, String time) {
        Log.d("DEBUG", "🔔 Hiển thị thông báo: " + title);

        // ✅ Kiểm tra nếu dữ liệu null, đặt giá trị mặc định
        if (title == null || title.isEmpty()) {
            title = "Thông báo mới";
        }
        if (body == null || body.isEmpty()) {
            body = "Bạn có dữ liệu mới.";
        }
        if (time == null || time.isEmpty()) {
            time = "Không rõ thời gian";
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // ✅ Sử dụng BigTextStyle để hiển thị đầy đủ nội dung dài
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title + " (" + time + ")")
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body)) // ✅ Hiển thị nội dung dài hơn
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(id, builder.build());
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Kênh thông báo";
            String description = "Kênh thông báo cho ứng dụng";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void playSound() {
        Log.d("DEBUG", "🔊 Phát âm thanh cảnh báo!");
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound);
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }
}
