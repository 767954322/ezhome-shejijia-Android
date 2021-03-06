package com.autodesk.shejijia.shared.components.common.uielements.commentview.widget.viewgraph.shape;

import android.graphics.Path;

/**
 * @author Malidong .
 * @version 1.0 .
 *          created at: 2016/1/21 0021 15:18 .
 *          # update （update log） .
 *          <p/>
 *          + v1.0 .
 * @file user  .
 * @brief info .
 */
public abstract class BasePolygonShape implements PolygonShape {

    private Path mPath;
    private PolygonShapeSpec polygonShapeSpec;

    public BasePolygonShape() {
        this.mPath = new Path();
    }

    /**
     * Return a valid closed path
     *
     * @param spec shape specs
     * @return a Path
     */
    @Override
    public Path getPolygonPath(PolygonShapeSpec spec) {
        float pointX, pointY, rotatedPointX, rotatedPointY, currentPointX = 0f, currentPointY = 0f;
        double angleRadians = Math.toRadians(spec.getRotation());
        polygonShapeSpec = spec;

        mPath.reset();
        int i = 0;
        do {
            //next vertex point
            pointX = spec.getCenterX() + spec.getDiameter() / 2f *
                    (float) Math.cos(2 * Math.PI * i / spec.getNumVertex());
            pointY = spec.getCenterY() + spec.getDiameter() / 2f *
                    (float) Math.sin(2 * Math.PI * i / spec.getNumVertex());
            //rotate vertex
            rotatedPointX = (float) (Math.cos(angleRadians) * (pointX - spec.getCenterX()) -
                    Math.sin(angleRadians) * (pointY - spec.getCenterY()) + spec.getCenterX());
            rotatedPointY = (float) (Math.sin(angleRadians) * (pointX - spec.getCenterX()) +
                    Math.cos(angleRadians) * (pointY - spec.getCenterY()) + spec.getCenterY());

            if (i == 0) { //move to first vertex
                mPath.moveTo(rotatedPointX, rotatedPointY);
            } else {
                //how to draw to next point
                addEffect(currentPointX, currentPointY, rotatedPointX, rotatedPointY);
            }

            currentPointX = rotatedPointX;
            currentPointY = rotatedPointY;
            i++;
        } while (i <= spec.getNumVertex());
        mPath.close();

        return mPath;
    }

    public Path getPath() {
        return mPath;
    }

    public PolygonShapeSpec getPolygonShapeSpec() {
        return polygonShapeSpec;
    }

    /**
     * Indicates how to draw to next point
     *
     * @param currentX current point x
     * @param currentY current point y
     * @param nextX    next point x
     * @param nextY    next point y
     */
    abstract protected void addEffect(float currentX, float currentY, float nextX, float nextY);

}
