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

import java.util.Random;

import javax.vecmath.Point3i;

import com.hellblazer.boids.behavior.FlockingBehavior;
import com.hellblazer.geometry.Vector3i;
import com.hellblazer.primeMover.Entity;
import com.hellblazer.primeMover.NonEvent;
import com.hellblazer.thoth.Cursor;
import com.hellblazer.thoth.Perceiving;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

@Entity({ Boid.class })
abstract public class BoidImpl<Flock extends Perceiving> implements Boid<Flock> {
    protected Point3i field;
    protected Animation animation;
    protected int maximumSpeed;
    protected Vector3i velocity = new Vector3i();
    protected Cursor locator;
    protected FlockingBehavior<Flock> behavior;
    protected double slowDown = 0.05;
    protected double randomChange;
    protected God god;

    protected static Random RANDOM = new Random(666);

    public BoidImpl(FlockingBehavior<Flock> behavior, Vector3i initialVelocity,
                    int maximumSpeed, Animation animation, Point3i field,
                    God god) {
        this.behavior = behavior;
        this.velocity = initialVelocity;
        this.animation = animation;
        this.maximumSpeed = maximumSpeed;
        this.field = field;
        this.god = god;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.boids.Boid#eatenBy(com.hellblazer.boids.Boid)
     */
    @Override
    public void eatenBy(Boid<?> predator) {
        god.died(this);
    }

    @Override
    public void fade(Perceiving neighbor) {
        if (equals(neighbor)) {
            System.out.println("*****");
        }
        behavior.fade(neighbor);
    }

    @Override
    public void move(Perceiving neighbor, Point3i location, Vector3i velocity) {
        if (equals(neighbor)) {
            System.out.println("*****");
        }
        behavior.move(neighbor, location, velocity);
    }

    @Override
    public void notice(Perceiving neighbor, Point3i location) {
        behavior.notice(neighbor, location);
    }

    @Override
    public void setCursor(Cursor locator) {
        this.locator = locator;
    }

    @NonEvent
    public void setRandomChange(double randomChange) {
        if (randomChange < 0.0 || randomChange > 1.0) {
            throw new IllegalArgumentException(
                                               "Random change factor must be between 0 and 1");
        }
        this.randomChange = randomChange;
    }

    @NonEvent
    public void setSlowDown(double slowDown) {
        this.slowDown = slowDown;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.boids.Boid#step()
     */
    @Override
    public void step() {
        Vector3i newVelocity = new Vector3i(velocity);
        newVelocity.add(behavior.getFlockingVector(locator.getLocation(),
                                                   velocity, maximumSpeed));
        newVelocity.add(boundPosition(locator.getLocation()));
        newVelocity.add(getRandomVector(locator.getLocation()));
        velocity = newVelocity;
        updatePosition();
    }

    /**
     * @param position
     * @return the vector required to keep within the field bounds
     */
    protected Vector3i boundPosition(Point3i position) {
        Vector3i vector = new Vector3i();
        int boundaryDistance = 10 * maximumSpeed;
        int delta = maximumSpeed / 4;
        if (position.x < boundaryDistance) {
            vector.x = delta;
        } else if (position.x > field.x - boundaryDistance) {
            vector.x = -delta;
        }
        if (position.y < boundaryDistance) {
            vector.y = delta;
        } else if (position.y > field.y - boundaryDistance) {
            vector.y = -delta;
        }
        return vector;
    }

    protected int getMaximumSpeed() {
        return maximumSpeed;
    }

    protected Vector3i getRandomVector(Point3i currentPosition) {
        Vector3i vector = new Vector3i(RANDOM.nextInt(maximumSpeed * 2)
                                       - maximumSpeed,
                                       RANDOM.nextInt(maximumSpeed * 2)
                                               - maximumSpeed,
                                       RANDOM.nextInt(maximumSpeed * 2)
                                               - maximumSpeed);
        vector.scale(randomChange);
        return vector;
    }

    protected void updatePosition() {
        velocity.z = 0;
        if (velocity.length() > getMaximumSpeed()) {
            velocity.normalizeTo(getMaximumSpeed());
        }
        Point3i oldPosition = new Point3i(locator.getLocation());
        Point3i newPosition = locator.getLocation();
        newPosition.add(velocity);

        if (newPosition.x < 50 && newPosition.x < oldPosition.x) {
            //newPosition.x = 50;
            velocity.x += getMaximumSpeed() / 5;
        } else if (newPosition.x > field.x - 50
                   && newPosition.x > oldPosition.x) {
            //newPosition.x = field.x - 50;
            velocity.x -= getMaximumSpeed() / 5;
        }

        if (newPosition.y < 50 && newPosition.y < oldPosition.y) {
            //newPosition.y = 50;
            velocity.y += getMaximumSpeed() / 5;
        } else if (newPosition.y > field.y - 50
                   && newPosition.y > oldPosition.y) {
            //newPosition.y = field.y - 50;
            velocity.y -= getMaximumSpeed() / 5;
        }

        if (velocity.length() > getMaximumSpeed()) {
            velocity.normalizeTo(getMaximumSpeed());
        }

        /**
         * double scale = Math.max(Math.abs(velocity.x) * slowDown, 0.01);
         * velocity.x -= velocity.x / scale; scale =
         * Math.max(Math.abs(velocity.y) * slowDown, 0.01); velocity.y -=
         * velocity.y / scale;
         */
        locator.moveBy(velocity);
        animation.update(locator.getLocation());
    }
}
