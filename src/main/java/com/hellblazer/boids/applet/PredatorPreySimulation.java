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

package com.hellblazer.boids.applet;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.vecmath.Point3i;

import com.hellblazer.boids.Animation;
import com.hellblazer.boids.BoidImpl;
import com.hellblazer.boids.God;
import com.hellblazer.boids.Predator;
import com.hellblazer.boids.Prey;
import com.hellblazer.boids.behavior.FlockingPrey;
import com.hellblazer.boids.behavior.PredatorPack;
import com.hellblazer.geometry.Vector3i;
import com.hellblazer.primeMover.Kronos;
import com.hellblazer.primeMover.controllers.SteppingController;
import com.hellblazer.primeMover.runtime.Framework;
import com.hellblazer.thoth.Perceiving;
import com.hellblazer.thoth.impl.Perceptron;

/**
 * 
 * @author <a href="mailto:hal.hildebrand@gmail.com">Hal Hildebrand</a>
 * 
 */

public class PredatorPreySimulation extends Applet implements KeyListener,
        MouseListener, God {
    class RefreshTask extends TimerTask {

        @Override
        public void run() {
            Framework.setController(controller);
            synchronized (sync) {
                Kronos.sleep(1000);
                if (next_ready) {
                    for (Perceptron<BoidImpl<?>> p : predators.keySet()) {
                        p.getSim().step();
                    }
                    for (Perceptron<BoidImpl<?>> p : prey.keySet()) {
                        p.getSim().step();
                    }
                    try {
                        controller.step();
                    } catch (Throwable e) {
                        throw new IllegalStateException(
                                                        "Unable to step controller",
                                                        e);
                    }
                    next_ready = false;
                    repaint();
                }
                if (!step_mode) {
                    next_ready = true;
                }
            }
        }
    }

    private static final long serialVersionUID = 1L;
    protected static final int scale = 100;
    protected static int x = 1500;
    protected static int y = 1000;

    protected static final Point3i field = new Point3i(x * scale, y * scale, 0);
    protected Map<Perceptron<BoidImpl<?>>, Animation> predators = new HashMap<Perceptron<BoidImpl<?>>, Animation>();
    protected Map<Perceptron<BoidImpl<?>>, Animation> prey = new HashMap<Perceptron<BoidImpl<?>>, Animation>();

    protected Random random;
    protected int numOfPrey = 24;
    protected int maximumPreyFlockDistance = 20 * scale;
    protected int preyScareDistance = 80 * scale;
    protected int preyAoiRadius = 100 * scale;
    protected int maxPreySpeed = 4 * scale;
    protected double preyCohesionChange = 0.002;
    protected double preyRepellerChange = 0.002;
    protected double preyVeocityMatchChange = 0.002;
    protected double predatorFleeChange = 0.04;

    protected double preyRandomChange = 0.003;
    protected int numOfPredators = 2;
    protected int maximumPredatorFlockDistance = 100 * scale;
    protected int predatorAoiRadius = 200 * scale;
    protected int maxPredatorSpeed = 3 * scale;
    protected int maxPreditorChaseSpeed = 5 * scale;
    protected int predatorCatchDistance = 5 * scale;
    protected int predatorSeekRadius = 100 * scale;
    protected double predatorCohesionChange = 0.0001;
    protected double predatorRepellerChange = 0.001;
    protected double predatorVeocityMatchChange = 0.001;
    protected double predatorHuntChange = 0.01;

    protected double predatorRandomChange = 0.0;
    protected final Object sync = new Object();
    protected int delay = 1000 / 45;
    protected boolean next_ready = true;
    protected SteppingController controller;
    protected boolean step_mode = true;
    protected Timer timer;
    protected RefreshTask updateTask;
    protected Graphics2D offGraphics;
    protected Image offImage;
    protected boolean showAoi = true;
    protected boolean showEdges = true;

    protected Perceptron<BoidImpl<?>> selected;

    @Override
    public void died(Perceiving dead) {
        Perceptron<BoidImpl<?>> deadPerceptron = null;
        synchronized (sync) {
            for (Perceptron<BoidImpl<?>> perceptron : prey.keySet()) {
                if (dead.equals(perceptron.getSim())) {
                    deadPerceptron = perceptron;
                    break;
                }
            }
            if (deadPerceptron != null) {
                deadPerceptron.leave();
                prey.remove(deadPerceptron);
            }
        }
    }

    @Override
    public void init() {
        controller = new SteppingController();
        Framework.setController(controller);
        controller.setCurrentTime(0);
        random = new Random(667);
        setSize(field.x / scale, field.y / scale);
        setBackground(Color.white);
        setForeground(Color.black);

        Perceptron<?> gateway = null;
        for (int i = 0; i < numOfPrey; i++) {
            Perceptron<BoidImpl<?>> boid = makePrey(i);
            if (i == 0) {
                gateway = boid;
            }
            boid.join(gateway);
        }

        for (int i = 0; i < numOfPredators; i++) {
            Perceptron<BoidImpl<?>> boid = makePredator(i + numOfPrey);
            boid.join(gateway);
        }

        // preyAnim.get(0).setSelected(true);  
        addKeyListener(this);
        addMouseListener(this);
        validate();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        // System.out.println("keycode=" + keyCode);
        // up, down, left, right
        switch (keyCode) {
            // ' ' (space)
            case 32:
                if (step_mode == true) {
                    next_ready = true;
                } else {
                    step_mode = true;
                }
                break;
            // enter (step-mode toggle)
            case 10:
                step_mode = !step_mode;
                break;
            // 'a'
            case 65:
                showAoi = !showAoi;
                break;
            // 'e'
            case 69:
                showEdges = !showEdges;
                break;
            // 'q'
            case 81:
                break;
        }
        if (keyCode >= 37 && keyCode <= 40 && !step_mode) {
            return;
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }
        for (Perceptron<BoidImpl<?>> perceptron : prey.keySet()) {
            Vector3i distance = new Vector3i(perceptron.getLocation());
            distance.sub(new Point3i(e.getX() * scale, e.getY() * scale, 0));
            if (distance.length() < 10 * scale) {
                if (selected != null) {
                    if (prey.get(selected) != null) {
                        prey.get(selected).setSelected(false);
                    } else {
                        predators.get(selected).setSelected(false);
                    }
                }
                selected = perceptron;
                prey.get(selected).setSelected(true);
                repaint();
                return;
            }
        }
        for (Perceptron<BoidImpl<?>> perceptron : predators.keySet()) {
            Vector3i distance = new Vector3i(perceptron.getLocation());
            distance.sub(new Point3i(e.getX() * scale, e.getY() * scale, 0));
            if (distance.length() < 10 * scale) {
                if (selected != null) {
                    if (prey.get(selected) != null) {
                        prey.get(selected).setSelected(false);
                    } else {
                        predators.get(selected).setSelected(false);
                    }
                }
                selected = perceptron;
                predators.get(selected).setSelected(true);
                repaint();
                return;
            }
        }
        if (selected != null) {
            if (prey.get(selected) != null) {
                prey.get(selected).setSelected(false);
            } else {
                predators.get(selected).setSelected(false);
            }
            selected = null;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void paint(Graphics g) {
        if (offGraphics != null) {
            g.drawImage(offImage, 0, 0, null);
        }
    }

    @Override
    public void start() {
        timer = new java.util.Timer();
        updateTask = new RefreshTask();
        timer.schedule(updateTask, 0, delay);
    }

    @Override
    public void update(Graphics g) {
        if (offGraphics == null) {
            offImage = createImage(x, y);
            offGraphics = (Graphics2D) offImage.getGraphics();

        }
        synchronized (sync) {
            offGraphics.setColor(getBackground());
            offGraphics.fillRect(0, 0, x, y);
            offGraphics.setColor(Color.BLACK);
            offGraphics.drawRect(0, 0, x, y);
            for (Animation anim : predators.values()) {
                anim.draw(showEdges, showAoi, offGraphics);
            }
            for (Animation anim : prey.values()) {
                anim.draw(showEdges, showAoi, offGraphics);
            }
            g.drawImage(offImage, 0, 0, null);

        }
    }

    protected Perceptron<BoidImpl<?>> createPerceptron(Point3i position,
                                                       UUID id,
                                                       BoidImpl<?> boid,
                                                       int aoiRadius,
                                                       int maximumSpeed) {
        return new Perceptron<BoidImpl<?>>(boid, id, position, aoiRadius,
                                           maximumSpeed, true);
    }

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Perceptron<BoidImpl<?>> makePredator(int num) {
        Point3i position = new Point3i(random.nextInt(field.x),
                                       random.nextInt(field.y), 0);
        Vector3i velocity = new Vector3i(
                                         maxPredatorSpeed
                                                 - random.nextInt(maxPredatorSpeed)
                                                 * 2,
                                         maxPredatorSpeed
                                                 - random.nextInt(maxPredatorSpeed)
                                                 * 2, 0);
        velocity.normalizeTo(maxPredatorSpeed);
        PredatorPack<Predator<?, ?>, Prey<?, ?>> behavior = new PredatorPack<Predator<?, ?>, Prey<?, ?>>();
        behavior.setFlockClass(Predator.class);
        behavior.setPreyClass(Prey.class);
        behavior.setCohesionChange(predatorCohesionChange);
        behavior.setMaximumFlockDistance(maximumPredatorFlockDistance);
        behavior.setRepellerChange(predatorRepellerChange);
        behavior.setVelocityMatchChange(predatorVeocityMatchChange);
        behavior.setHuntChange(predatorHuntChange);
        Color color = new Color((int) (random.nextDouble() * 256 * 256 * 256));
        Animation animation = new Animation(scale, 10 * scale, 10 * scale,
                                            color, 2);
        BoidImpl<?> predator = new Predator(behavior, velocity, maxPreySpeed,
                                            animation, field,
                                            predatorSeekRadius,
                                            predatorCatchDistance,
                                            maxPreditorChaseSpeed, this);
        predator.setRandomChange(predatorRandomChange);
        Perceptron perceptron = createPerceptron(position, new UUID(0, num),
                                                 predator, predatorAoiRadius,
                                                 maxPreditorChaseSpeed);
        animation.setPerceptron(perceptron);
        predator.setCursor(perceptron);
        predators.put(perceptron, animation);
        return perceptron;
    }

    @SuppressWarnings("unchecked")
    protected Perceptron<BoidImpl<?>> makePrey(int num) {
        Point3i position = new Point3i(random.nextInt(field.x),
                                       random.nextInt(field.y), 0);
        Vector3i velocity = new Vector3i(maxPreySpeed
                                         - random.nextInt(maxPreySpeed) * 2,
                                         maxPreySpeed
                                                 - random.nextInt(maxPreySpeed)
                                                 * 2, 0);
        velocity.normalizeTo(maxPreySpeed);
        FlockingPrey<Prey<?, ?>, Predator<?, ?>> behavior = new FlockingPrey<Prey<?, ?>, Predator<?, ?>>();
        behavior.setFlockClass(Prey.class);
        behavior.setPredatorClass(Predator.class);
        behavior.setCohesionChange(preyCohesionChange);
        behavior.setMaximumFlockDistance(maximumPreyFlockDistance);
        behavior.setRepellerChange(preyRepellerChange);
        behavior.setVelocityMatchChange(preyVeocityMatchChange);
        behavior.setScareDistance(preyScareDistance);
        behavior.setPredatorFleeChange(predatorFleeChange);
        Color color = new Color((int) (random.nextDouble() * 256 * 256 * 256));
        Animation animation = new Animation(scale, 5 * scale, 5 * scale, color,
                                            1);
        BoidImpl<?> p = new Prey(behavior, velocity, maxPreySpeed, animation,
                                 field, this);
        p.setRandomChange(preyRandomChange);
        Perceptron perceptron = createPerceptron(position, new UUID(0, num), p,
                                                 preyAoiRadius,
                                                 maxPredatorSpeed);
        animation.setPerceptron(perceptron);
        p.setCursor(perceptron);
        prey.put(perceptron, animation);
        return perceptron;
    }
}
