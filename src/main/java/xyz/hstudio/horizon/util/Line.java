package xyz.hstudio.horizon.util;

public class Line {

    public double height;
    public double slope;

    public Line(double height, double slope) {
        this.height = height;
        this.slope = slope;
    }

    public double getYatX(double x) {
        return slope * x + height;
    }
}