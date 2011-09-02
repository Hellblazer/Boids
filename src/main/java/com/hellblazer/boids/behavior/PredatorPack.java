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

package com.hellblazer.boids.behavior;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point3i;

import com.hellblazer.geometry.Vector3i;
import com.hellblazer.thoth.Perceiving;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class PredatorPack<Pack extends Perceiving, Prey extends Perceiving>
        extends FlockingBehavior<Pack> {
    protected Map<Prey, Point3i> prey = new HashMap<Prey, Point3i>();
    protected Class<?> preyClass;
    protected Point3i preyPositionSum = new Point3i();
    protected double huntChange;

    @Override
    public void fade(Perceiving neighbor) {
        super.fade(neighbor);
        Point3i lastPosition = prey.remove(neighbor);
        if (lastPosition != null) {
            preyPositionSum.sub(lastPosition);
        }
    }

    @Override
    public Vector3i getFlockingVector(Point3i currentPosition,
                                      Vector3i currentVelocity, int maximumSpeed) {
        Vector3i vector = super.getFlockingVector(currentPosition,
                                                  currentVelocity, maximumSpeed);
        Vector3i huntVector = getHuntVector(currentPosition, maximumSpeed);
        vector.sub(huntVector);
        return vector;
    }

    public double getHuntChange() {
        return huntChange;
    }

    public Point3i getPosition(Prey p) {
        return prey.get(p);
    }

    public Map<Prey, Point3i> getPrey() {
        return Collections.unmodifiableMap(prey);
    }

    public Class<?> getPreyClass() {
        return preyClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void move(Perceiving neighbor, Point3i location, Vector3i velocity) {
        if (preyClass.isAssignableFrom(neighbor.getClass())) {
            Point3i previous = prey.put((Prey) neighbor, location);
            if (previous != null) {
                preyPositionSum.sub(previous);
                preyPositionSum.add(location);
            }
        } else {
            super.move(neighbor, location, velocity);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void notice(Perceiving neighbor, Point3i location) {
        if (preyClass.isAssignableFrom(neighbor.getClass())) {
            prey.put((Prey) neighbor, location);
            preyPositionSum.add(location);
        } else {
            super.notice(neighbor, location);
        }
    }

    public void setHuntChange(double huntChange) {
        if (huntChange < 0 || huntChange > 1) {
            throw new IllegalStateException(
                                            "Hunt change factor must be between 0 and 1");
        }
        this.huntChange = huntChange;
    }

    public void setPreyClass(Class<?> preyClass) {
        this.preyClass = preyClass;
    }

    protected Vector3i getHuntVector(Point3i currentPosition, int maximumSpeed) {
        if (prey.size() == 0) {
            return new Vector3i();
        }
        Vector3i avg = new Vector3i(preyPositionSum);
        avg.scaleInverse(prey.size());
        Vector3i vector = new Vector3i(currentPosition);
        vector.sub(avg);
        if (vector.length() > maximumSpeed) {
            vector.normalizeTo(maximumSpeed);
        }
        vector.scale(huntChange);
        return vector;
    }
}
