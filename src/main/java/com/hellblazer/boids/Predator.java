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

import java.util.Map;

import javax.vecmath.Point3i;

import com.hellblazer.boids.behavior.PredatorPack;
import com.hellblazer.geometry.Vector3i;
import com.hellblazer.thoth.Perceiving;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class Predator<F extends Boid<?>, P extends Boid<?>> extends BoidImpl<F> {
    protected PredatorPack<F, P> behavior;
    protected int                catchDistance;
    protected int                maximumChaseSpeed;
    protected int                seekRadius;
    protected int                stamina;
    protected P                  target;

    public Predator(PredatorPack<F, P> behavior, Vector3i initialVelocity, int maximumSpeed, Animation animation,
                    Point3i field, int seekRadius, int catchDistance, int maximumChaseSpeed, God god) {
        super(behavior, initialVelocity, maximumSpeed, animation, field, god);
        this.seekRadius = seekRadius;
        this.behavior = behavior;
        this.catchDistance = catchDistance;
        this.maximumChaseSpeed = maximumChaseSpeed;
    }

    @Override
    public void fade(Perceiving neighbor) {
        if (neighbor.equals(target)) {
            target = null;
        }
        super.fade(neighbor);
    }

    @Override
    public void step() {
        if (target != null) {
            pursueTarget(behavior.getPosition(target));
        } else {
            if (stamina < 100) {
                stamina += 1;
            }
            findTarget();
            if (target != null && stamina > 99) {
                stamina = 100;
                pursueTarget(behavior.getPosition(target));
            } else {
                super.step();
            }
        }
    }

    protected boolean closeForKill(Point3i targetLocation) {
        Vector3i distance = new Vector3i(locator.getLocation());
        distance.sub(targetLocation);
        return distance.length() <= catchDistance;
    }

    protected void findTarget() {
        for (Map.Entry<P, Point3i> entry : behavior.getPrey().entrySet()) {
            Vector3i distance = new Vector3i(locator.getLocation());
            distance.sub(entry.getValue());
            if (distance.length() <= seekRadius) {
                target = entry.getKey();
            }
        }
    }

    @Override
    protected int getMaximumSpeed() {
        if (target != null) {
            return maximumChaseSpeed;
        } else {
            return maximumSpeed;
        }
    }

    protected Vector3i getPursuitVector() {
        Vector3i vector = new Vector3i(behavior.getPosition(target));
        vector.sub(locator.getLocation());
        vector.normalizeTo(maximumChaseSpeed);
        return vector;
    }

    protected void pursueTarget(Point3i targetLocation) {
        stamina -= 1;
        if (closeForKill(targetLocation)) {
            target.eatenBy(this);
            target = null;
            super.step();
        } else {
            if (stamina < 1) {
                target = null;
                super.step();
            } else {
                Vector3i delta = new Vector3i();
                Vector3i boundPosition = boundPosition(locator.getLocation());
                Vector3i pursuitVector = getPursuitVector();
                delta.add(boundPosition);
                delta.add(pursuitVector);
                velocity.add(delta);
                if (velocity.length() > maximumChaseSpeed) {
                    velocity.normalizeTo(maximumChaseSpeed);
                }
                updatePosition();
            }
        }
    }
}
