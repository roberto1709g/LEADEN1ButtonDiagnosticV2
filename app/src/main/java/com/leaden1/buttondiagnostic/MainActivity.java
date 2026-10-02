package com.leaden1.buttondiagnostic;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private TextView status, lastEvent, history;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        status=findViewById(R.id.status);
        lastEvent=findViewById(R.id.lastEvent);
        history=findViewById(R.id.history);

        ((Button)findViewById(R.id.clearButton)).setOnClickListener(v -> {
            DiagnosticMediaService.clearHistory();
            refresh();
        });

        DiagnosticMediaService.setUiCallback(this::refresh);
        ContextCompat.startForegroundService(this,
                new Intent(this, DiagnosticMediaService.class));
        DiagnosticMediaService.appendExternalEvent("APP_INICIADA_V2");
        refresh();
    }

    @Override protected void onResume() {
        super.onResume();
        DiagnosticMediaService.setUiCallback(this::refresh);
        refresh();
    }

    @Override protected void onDestroy() {
        DiagnosticMediaService.setUiCallback(null);
        super.onDestroy();
    }

    private void refresh() {
        runOnUiThread(() -> {
            status.setText(DiagnosticMediaService.isSessionActive()
                    ? "● MEDIA SESSION ACTIVA" : "○ MEDIA SESSION INACTIVA");
            lastEvent.setText("ÚLTIMO EVENTO: "+DiagnosticMediaService.getLastEvent());
            history.setText("HISTORIAL\n"+DiagnosticMediaService.getHistory());
        });
    }
}
