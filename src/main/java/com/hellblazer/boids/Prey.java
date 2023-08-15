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

import javax.vecmath.Point3i;

import com.hellblazer.boids.behavior.FlockingPrey;
import com.hellblazer.geometry.Vector3i;
import com.hellblazer.thoth.Perceiving;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class Prey<F extends Perceiving, P extends Perceiving> extends BoidImpl<F> {
    FlockingPrey<F, P> behavior;

    public Prey(FlockingPrey<F, P> behavior, Vector3i initialVelocity, int maximumSpeed, Animation animation,
                Point3i field, God god) {
        super(behavior, initialVelocity, maximumSpeed, animation, field, god);
        this.behavior = behavior;
    }
}
