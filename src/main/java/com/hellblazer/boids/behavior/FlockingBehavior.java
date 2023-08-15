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

public class FlockingBehavior<Flock extends Perceiving> {
    public static class FlockState {
        public final Point3i  position;
        public final Vector3i velocity;

        public FlockState(Point3i position, Vector3i velocity) {
            this.position = position;
            this.velocity = velocity;
        }
    }

    protected double                 cohesionChange;
    protected Map<Flock, FlockState> flock       = new HashMap<Flock, FlockState>();
    protected Class<?>               flockClass;
    protected int                    maximumFlockDistance;
    protected Point3i                positionSum = new Point3i();
    protected double                 repellerChange;
    protected double                 velocityMatchChange;

    protected Vector3i velocitySum = new Vector3i();

    public void fade(Perceiving neighbor) {
        FlockState previousState = flock.remove(neighbor);
        if (previousState != null) {
            positionSum.sub(previousState.position);
            velocitySum.sub(previousState.velocity);
        }
    }

    public double getCohesionChange() {
        return cohesionChange;
    }

    public Map<Flock, FlockState> getFlock() {
        return Collections.unmodifiableMap(flock);
    }

    public Class<?> getFlockClass() {
        return flockClass;
    }

    /**
     * @param currentPosition
     * @param currentVelocity
     * @param maximumSpeed
     * @return Answer the velocity change necessary to maintain membership with the
     *         flock
     */
    public Vector3i getFlockingVector(Point3i currentPosition, Vector3i currentVelocity, int maximumSpeed) {
        Vector3i vector = new Vector3i();
        vector.add(getCohesionVector(currentPosition));
        vector.sub(getRepellerVector(currentPosition, maximumSpeed));
        vector.sub(getVelocityMatchVector(currentVelocity));
        return vector;
    }

    public FlockState getFlockState(Flock flockMate) {
        return flock.get(flockMate);
    }

    public int getMaximumFlockDistance() {
        return maximumFlockDistance;
    }

    public double getRepellerChange() {
        return repellerChange;
    }

    public double getVelocityMatchChange() {
        return velocityMatchChange;
    }

    @SuppressWarnings("unchecked")
    public void move(Perceiving neighbor, Point3i location, Vector3i velocity) {
        if (flockClass.isAssignableFrom(neighbor.getClass())) {
            FlockState previousState = flock.put((Flock) neighbor, new FlockState(location, velocity));
            if (previousState != null) {
                positionSum.sub(previousState.position);
                velocitySum.sub(previousState.velocity);
                positionSum.add(location);
                velocitySum.add(velocity);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void notice(Perceiving neighbor, Point3i location) {
        if (flockClass.isAssignableFrom(neighbor.getClass())) {
            flock.put((Flock) neighbor, new FlockState(location, new Vector3i()));
            positionSum.add(location);
        }
    }

    public void setCohesionChange(double cohesionChange) {
        if (cohesionChange > 1.0 || cohesionChange < 0.0) {
            throw new IllegalArgumentException("Cohesion change factor must be {0..1}");
        }
        this.cohesionChange = cohesionChange;
    }

    public void setFlockClass(Class<?> flockClass) {
        this.flockClass = flockClass;
    }

    public void setMaximumFlockDistance(int maximumFlockDistance) {
        this.maximumFlockDistance = maximumFlockDistance;
    }

    public void setRepellerChange(double repellerChange) {
        if (repellerChange > 1.0 || repellerChange < 0.0) {
            throw new IllegalArgumentException("Repeller change factor must be {0..1}");
        }
        this.repellerChange = repellerChange;
    }

    public void setVelocityMatchChange(double veocityMatchChange) {
        if (veocityMatchChange > 1.0 || veocityMatchChange < 0.0) {
            throw new IllegalArgumentException("Velocity match factor must be {0..1}");
        }
        this.velocityMatchChange = veocityMatchChange;
    }

    /**
     * @param currentPosition
     * @return the vector representing the change in direction required to keep
     *         flock cohesion
     */
    protected Vector3i getCohesionVector(Point3i currentPosition) {
        if (flock.size() == 0) {
            return new Vector3i();
        }
        Vector3i vector = new Vector3i(positionSum);
        vector.scaleInverse(flock.size());
        vector.sub(currentPosition);
        vector.scale(cohesionChange);
        return vector;
    }

    /**
     * @param currentPosition
     * @return the vector representing the change in velocity necessary to keep from
     *         running into members of the flock
     */
    protected Vector3i getRepellerVector(Point3i currentPosition, int maximumSpeed) {
        Vector3i repeller = new Vector3i();
        for (FlockState state : flock.values()) {
            Vector3i v = new Vector3i(currentPosition);
            v.sub(state.position);
            if (v.length() <= maximumFlockDistance) {
                Point3i p = new Point3i(currentPosition);
                p.sub(state.position);
                repeller.sub(p);
            }
        }
        repeller.scale(repellerChange);
        return repeller;
    }

    /**
     * @param currentVelocity
     * @return the vector representing the change in velocity required to match the
     *         flock's velocity
     */
    protected Vector3i getVelocityMatchVector(Vector3i currentVelocity) {
        if (flock.size() == 0) {
            return new Vector3i();
        }
        Vector3i vector = new Vector3i(velocitySum);
        vector.scaleInverse(flock.size());
        vector.sub(currentVelocity);
        vector.scale(velocityMatchChange);
        return vector;
    }
}
