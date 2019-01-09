package com.example.piotrjanus.sensorapp;

import android.graphics.PointF;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class WriteToFile {

    public static void saveToTxt(String fileName, ArrayList<PointF> graphList){
        File root = Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/data");
        dir.mkdirs();
        File file = new File(dir,fileName);

        try(FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            PrintWriter printWriter = new PrintWriter(fileOutputStream);
            StringBuilder values = new StringBuilder();

            double firstTime = graphList.get(0).x;
            for (PointF pointF : graphList) {
                values.append(pointF.x - firstTime + ";" + pointF.y + "\n");
            }

            printWriter.println(values.toString());
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
