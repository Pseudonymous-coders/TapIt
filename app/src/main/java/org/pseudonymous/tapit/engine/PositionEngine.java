package org.pseudonymous.tapit.engine;

import android.graphics.Point;

import org.pseudonymous.tapit.components.Circle;
import org.pseudonymous.tapit.configs.CircleProps;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * Created by smerkous on 9/14/17.
 */

public class PositionEngine {
    private Engine engine;
    private int sides = 3, point = 0;
    private List<Point> points;
    private boolean staticMode = false;

    public PositionEngine(Engine engine) {
        this.engine = engine;
    }

    public void setSides(int sides) {
        this.setSides(sides, (2 * (float) Math.PI * ThreadLocalRandom.current().nextFloat()));
    }

    public void setSides(int sides, float startAngle) {
        if(sides < 3) sides = 3;
        this.sides = sides;
        this.points = new CopyOnWriteArrayList<>();
        float centerAngle = (2 * (float) Math.PI) / sides;
        for(int ind = 0; ind < sides; ind++) {
            float ang = startAngle + (ind * centerAngle);
            int xPos = (int) Math.round(50f + (50f * Math.cos(ang)));
            int yPos = (int) Math.round(50f + (50f * Math.sin(ang)));
            this.points.add(new Point(xPos, yPos));
        }
    }

    public void setSidesStatic(int sides) {
        float startAngle = ((float) Math.PI / 2);
        if(sides % 2 != 0) {
            startAngle += (2 * (float) Math.PI / sides);
        }
        staticMode = true;
        this.setSides(sides, startAngle);
    }

    public void rebasePoints() {
        this.point = 0;
    }

    public Point getNextPoint(Circle circle, CircleProps props, float padding) {
        Point ret = new Point();
        float paddingWidth = circle.getPaddingWidth(padding);
        float paddingHeight = circle.getPaddingHeight(padding);
        float availWidth = circle.getAvailableWidth(paddingWidth);
        float availHeight = circle.getAvailableHeight(paddingHeight);
        boolean shiftMode = false;
        if(staticMode) {
            if(availWidth < availHeight) {
                shiftMode = true;
                availHeight = availWidth;
            } else {
                availWidth = availHeight;
            }
        }

        try {
            ret.x = (int) (((float) this.points.get(this.point).x / 100f) * availWidth) + (int) paddingWidth;
            ret.y = (int) (((float) this.points.get(this.point).y / 100f) * availHeight) + (int) paddingHeight;
            if(staticMode) {
                if(shiftMode) {
                    float mDiff = (circle.getAvailableHeight(paddingHeight) - availHeight) / 2;
                    ret.y += mDiff;
                } else {
                    float mDiff = (circle.getAvailableWidth(paddingWidth) - availWidth) / 2;
                    ret.x += mDiff;
                }
            }
        } catch (Throwable ignored) {
            ret.x = (int) (0.5f * availWidth) + (int) paddingWidth;
            ret.y = (int) (0.5f * availHeight) + (int) paddingHeight;
        }
        this.point++;
        return ret;
    }
}
