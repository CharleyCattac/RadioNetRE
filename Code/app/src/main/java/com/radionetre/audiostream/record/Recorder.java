package com.radionetre.record;

import android.content.Context;
import android.widget.Toast;

import com.radionetre.play.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class Recorder {

    //singleton implementation
    private static Recorder instance = new Recorder();

    private Recorder() {
        player = Player.getInstance();
    }

    public static Recorder getInstance() {
        return instance;
    }

    private boolean isRecording;
    private Context context;
    private Player player;
    private Thread recordingThread;

    public void init(Context cont) {
        context = cont;
        isRecording = false;
        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                String path = context.getFilesDir().getAbsolutePath() + "/" + Calendar.getInstance().getTime() + ".mp3";
                System.out.println(path);
                File outputSource = new File(path);
                InputStream inputStream = null;
                FileOutputStream fileOutputStream = null;
                URL url = null;
                try {
                    url = new URL(player.getStream());
                    System.out.println(player.getStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    inputStream = url.openStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    fileOutputStream = new FileOutputStream(outputSource);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                int c;

                try {
                    while ((c = inputStream.read()) != -1 && isRecording) {
                        fileOutputStream.write(c);
                    }
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        });
    }

    public void record() {
        if (player.getMP()!= null && !isRecording) {
            Toast toast = Toast.makeText(context,
                    "Recording started", Toast.LENGTH_SHORT);
            toast.show();
            isRecording = true;
            if (recordingThread != null)
                recordingThread.start();
        } else if (recordingThread != null && isRecording && recordingThread.isAlive()) {
            Toast toast = Toast.makeText(context,
                    "Recording stopped", Toast.LENGTH_SHORT);
            toast.show();
            isRecording = false;
            recordingThread.interrupt();
        }
    }
}
