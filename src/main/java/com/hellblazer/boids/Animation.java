/**
 * Copyright (C) 2008 Hal Hildebrand. All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.hellblazer.boids;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point3i;

import com.hellblazer.thoth.Perceiving;
import com.hellblazer.thoth.impl.AbstractNode;
import com.hellblazer.thoth.impl.Perceptron;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

@SuppressWarnings("restriction")
public class Animation {
    protected boolean selected = false;
    protected Perceptron<?> perceptron;
    protected int tailScale = 1;
    protected int width, height, scale;
    protected Color color;
    protected Point3i position = new Point3i();
    protected Point3i[] oldPositions = new Point3i[] { new Point3i(),
                                                      new Point3i(),
                                                      new Point3i(),
                                                      new Point3i(),
                                                      new Point3i() };
    final static int node_radius = 5;
    final static BasicStroke stroke = new BasicStroke(2.0f);
    final static BasicStroke aoiStroke = new BasicStroke(3.0f);

    public Animation(int scale, int width, int height, Color color,
                     int tailScale) {
        this.scale = scale;
        this.width = width;
        this.height = height;
        this.color = color;
        this.tailScale = tailScale;
    }

    public void draw(boolean showEdges, boolean showAoi, Graphics2D graphics) {
        graphics.setStroke(stroke);
        graphics.setColor(color);
        for (int i = 0; i < 5; i++) {
            graphics.fillOval(scale(oldPositions[i].x) - i,
                              scale(oldPositions[i].y) - i, i * tailScale,
                              i * tailScale);
        }
        graphics.fillOval(scale(position.x - width / 2), scale(position.y
                                                               - height / 2),
                          scale(width), scale(height));
        if (selected) {
            graphics.setPaint(Color.red);
            for (AbstractNode<? extends Perceiving> neighbor : perceptron.getNeighbors()) {
                if (!neighbor.equals(perceptron)) {
                    graphics.draw(new Ellipse2D.Double(
                                                       scale(neighbor.getLocation().x)
                                                               - node_radius
                                                               * 2,
                                                       scale(neighbor.getLocation().y)
                                                               - node_radius
                                                               * 2,
                                                       node_radius * 2 * 2,
                                                       node_radius * 2 * 2));
                }
            }
            graphics.setPaint(Color.blue);
            graphics.draw(new Ellipse2D.Double(scale(position.x) - node_radius
                                               * 2, scale(position.y)
                                                    - node_radius * 2,
                                               node_radius * 2 * 2,
                                               node_radius * 2 * 2));
            graphics.setPaint(Color.red);
            if (showEdges) {
                List<Point2d[]> edges = perceptron.getVoronoiDomainEdges();
                for (Point2d[] edge : edges) {
                    graphics.draw(new Line2D.Double(scale(edge[0].x),
                                                    scale(edge[0].y),
                                                    scale(edge[1].x),
                                                    scale(edge[1].y)));
                }
            }
            if (showAoi) {
                graphics.setStroke(aoiStroke);
                graphics.draw(new Ellipse2D.Double(
                                                   scale(position.x
                                                         - perceptron.getAoiRadius()),
                                                   scale(position.y
                                                         - perceptron.getAoiRadius()),
                                                   scale(perceptron.getAoiRadius()) * 2,
                                                   scale(perceptron.getAoiRadius()) * 2));
                graphics.setStroke(stroke);
            }
        }
    }

    public void setPerceptron(Perceptron<?> perceptron) {
        this.perceptron = perceptron;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void update(Point3i newPosition) {
        for (int i = 0; i < 4; i++) {
            oldPositions[i] = new Point3i(oldPositions[i + 1]);
        }
        oldPositions[4] = new Point3i(position);
        position = newPosition;
    }

    protected double scale(double value) {
        return value / scale;
    }

    protected int scale(int value) {
        return value / scale;
    }
}
