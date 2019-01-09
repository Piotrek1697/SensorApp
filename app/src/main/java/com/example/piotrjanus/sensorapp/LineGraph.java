package com.example.piotrjanus.sensorapp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;

public class LineGraph {

    private XYSeries seriesX;
    private XYSeries seriesY;
    private XYSeries seriesZ;
    private XYSeriesRenderer rendererX = new XYSeriesRenderer();
    private XYSeriesRenderer rendererY = new XYSeriesRenderer();
    private XYSeriesRenderer rendererZ = new XYSeriesRenderer();
    private XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer multipleSeriesRenderer = new XYMultipleSeriesRenderer();
    private GraphicalView chartView;

    public LineGraph(String seriesXName, String seriesYName, String seriesZName) {
        seriesX = new XYSeries(seriesXName);
        seriesY = new XYSeries(seriesYName);
        seriesZ = new XYSeries(seriesZName);

        dataset.addSeries(seriesX);
        dataset.addSeries(seriesY);
        dataset.addSeries(seriesZ);

        rendererX.setLineWidth(2);
        rendererX.setColor(Color.RED);
        rendererX.setPointStyle(PointStyle.CIRCLE);

        rendererY.setLineWidth(2);
        rendererY.setColor(Color.BLUE);
        rendererY.setPointStyle(PointStyle.CIRCLE);

        rendererZ.setLineWidth(2);
        rendererZ.setColor(Color.GREEN);
        rendererZ.setPointStyle(PointStyle.CIRCLE);

        multipleSeriesRenderer.setShowGrid(true);
        multipleSeriesRenderer.addSeriesRenderer(rendererX);
        multipleSeriesRenderer.addSeriesRenderer(rendererY);
        multipleSeriesRenderer.addSeriesRenderer(rendererZ);

    }

    public void addData(PointF pointF){
        seriesX.add(pointF.x,pointF.y);
    }

    public void addAllDataX(ArrayList<PointF> graphDataX){
        for (PointF pointF : graphDataX){
            seriesX.add(pointF.x,pointF.y);
        }
    }

    public void addAllDataY(ArrayList<PointF> graphDataY){
        for (PointF pointF : graphDataY){
            seriesY.add(pointF.x,pointF.y);
        }
    }

    public void addAllDataZ(ArrayList<PointF> graphDataZ){
        for (PointF pointF : graphDataZ){
            seriesZ.add(pointF.x,pointF.y);
        }
    }

    public GraphicalView getChartView(Context context) {
        chartView = ChartFactory.getLineChartView(context,dataset,multipleSeriesRenderer);
        return chartView;
    }

    public void clearData(){
        seriesX.clear();
        seriesY.clear();
        seriesZ.clear();

    }
}
