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

public class FlockingPrey<Flock extends Perceiving, Predator extends Perceiving> extends FlockingBehavior<Flock> {
    protected Class<?>               predatorClass;
    protected double                 predatorFleeChange;
    protected Point3i                predatorPositionSum = new Point3i();
    protected Map<Predator, Point3i> predators           = new HashMap<Predator, Point3i>();
    protected int                    scareDistance;

    @Override
    public void fade(Perceiving neighbor) {
        super.fade(neighbor);
        Point3i lastPosition = predators.remove(neighbor);
        if (lastPosition != null) {
            predatorPositionSum.sub(lastPosition);
        }
    }

    @Override
    public Vector3i getFlockingVector(Point3i currentPosition, Vector3i currentVelocity, int maximumSpeed) {
        Vector3i vector = super.getFlockingVector(currentPosition, currentVelocity, maximumSpeed);
        vector.sub(getPredatorFleeVector(currentPosition));
        return vector;
    }

    public Point3i getPosition(Predator predator) {
        return predators.get(predator);
    }

    public Class<?> getPredatorClass() {
        return predatorClass;
    }

    public double getPredatorFleeChange() {
        return predatorFleeChange;
    }

    public Map<Predator, Point3i> getPredators() {
        return Collections.unmodifiableMap(predators);
    }

    public int getScareDistance() {
        return scareDistance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void move(Perceiving neighbor, Point3i location, Vector3i velocity) {
        if (predatorClass.isAssignableFrom(neighbor.getClass())) {
            Point3i lastPosition = predators.put((Predator) neighbor, location);
            if (lastPosition != null) {
                predatorPositionSum.sub(lastPosition);
                predatorPositionSum.add(location);
            }
        } else {
            super.move(neighbor, location, velocity);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void notice(Perceiving neighbor, Point3i location) {
        if (predatorClass.isAssignableFrom(neighbor.getClass())) {
            predators.put((Predator) neighbor, location);
            predatorPositionSum.add(location);
        } else {
            super.notice(neighbor, location);
        }
    }

    public void setPredatorClass(Class<?> predatorClass) {
        this.predatorClass = predatorClass;
    }

    public void setPredatorFleeChange(double predatorFleeChange) {
        this.predatorFleeChange = predatorFleeChange;
    }

    public void setScareDistance(int scareDistance) {
        this.scareDistance = scareDistance;
    }

    /**
     * @param currentPosition
     * @return the vector which represents the velocity component which takes us
     *         away from our predators
     */
    protected Vector3i getPredatorFleeVector(Point3i currentPosition) {
        if (predators.size() == 0) {
            return new Vector3i();
        }
        Vector3i predatorVector = new Vector3i(predatorPositionSum);
        predatorVector.scaleInverse(predators.size());
        predatorVector.sub(currentPosition);
        if (predatorVector.length() < scareDistance) {
            predatorVector.scale(predatorFleeChange);
            return predatorVector;
        } else {
            return new Vector3i();
        }
    }
}
