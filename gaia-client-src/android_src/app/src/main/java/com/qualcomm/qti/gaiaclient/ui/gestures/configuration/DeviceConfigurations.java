/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures.configuration;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Action;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Configuration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GestureContext;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.QTILGestureContexts;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Touchpad;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeviceConfigurations {

    @NonNull
    final Map<GestureContext, Map<Touchpad, Action>> configurations = new LinkedHashMap<>();

    // assumption : the set has maximum one configuration per pair of (context, touchpad) - aka one action per pair
    public DeviceConfigurations(Set<Configuration> update) {
        if (update == null) {
            // empty list
            return;
        }

        // Sort configurations per contexts and transform configs left & right to both
        update.forEach(config -> {
            // get the set of configuration for the context of the configuration
            GestureContext context = config.getContext();
            Map<Touchpad, Action> configurations =
                    this.configurations.computeIfAbsent(context, c -> new LinkedHashMap<>());
            configurations.put(config.getTouchpad(), config.getAction());
        });

        configurations.values().forEach(DeviceConfigurations::sortConfigurations);
    }

    // private constructor for Generator
    private DeviceConfigurations(DeviceConfigurations origin) {
        if (origin == null) {
            return;
        }

        origin.configurations.forEach((originContext, originActions) -> {
            if (originActions != null && !originActions.isEmpty()) {
                Map<Touchpad, Action> actions = new LinkedHashMap<>();
                originActions.forEach((touchpad, action) -> {
                    if (action != null) {
                        actions.put(touchpad, action);
                    }
                });
                this.configurations.put(originContext, actions);
            }
        });
    }

    public boolean hasLeftOrRightConfiguration(GestureContext context) {
        Map<Touchpad, Action> configurations = this.configurations.get(context);
        return configurations != null &&
                (configurations.get(Touchpad.LEFT) != null || configurations.get(Touchpad.RIGHT) != null);
    }

    public boolean hasConfiguration(GestureContext context, Action action, Touchpad touchpad) {
        Map<Touchpad, Action> configs = configurations.get(context);
        Action currentAction = configs == null ? null : configs.get(touchpad);
        return currentAction != null && currentAction.equals(action);
    }

    public boolean hasNonGlobalConfigurations(Touchpad touchpad) {
        List<Touchpad> touchpads = new ArrayList<>();
        touchpads.add(touchpad);
        if (touchpad == Touchpad.LEFT || touchpad == Touchpad.RIGHT) {
            // Touchpad.LEFT/RIGHT is included in BOTH => check for BOTH as well
            touchpads.add(Touchpad.BOTH);
        }

        for (Map.Entry<GestureContext, Map<Touchpad, Action>> entry : configurations.entrySet()) {
            GestureContext context = entry.getKey();
            Map<Touchpad, Action> actions = entry.getValue();
            if (!context.equals(QTILGestureContexts.PASSTHROUGH_GLOBAL)) {
                if (touchpads.stream().anyMatch(t -> actions.get(t) != null)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean hasGlobalConfigurations(Touchpad touchpad) {
        List<Touchpad> touchpads = new ArrayList<>();
        touchpads.add(touchpad);
        if (touchpad == Touchpad.LEFT || touchpad == Touchpad.RIGHT) {
            // Touchpad.LEFT/RIGHT is included in BOTH => check for BOTH as well
            touchpads.add(Touchpad.BOTH);
        }

        for (Map.Entry<GestureContext, Map<Touchpad, Action>> entry : configurations.entrySet()) {
            GestureContext context = entry.getKey();
            Map<Touchpad, Action> actions = entry.getValue();
            if (context.equals(QTILGestureContexts.PASSTHROUGH_GLOBAL)) {
                if (touchpads.stream().anyMatch(t -> actions.get(t) != null)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void sortConfigurations(Map<Touchpad, Action> configurations) {
        Action leftAction = configurations.get(Touchpad.LEFT);
        Action rightAction = configurations.get(Touchpad.RIGHT);

        if (leftAction != null && leftAction.equals(rightAction)) {
            configurations.remove(Touchpad.LEFT);
            configurations.remove(Touchpad.RIGHT);
            configurations.put(Touchpad.BOTH, leftAction);
        }
    }

    public Map<Touchpad, Action> get(GestureContext context) {
        return configurations.get(context);
    }

    public static class Generator {

        /**
         * @param origin
         *         the configurations that needs to be updated.
         * @param context
         *         the gesture context to update.
         * @param touchpad
         *         the touchpad for which the action needs to be updated.
         * @param action
         *         the action to update the touchpad with, if null, all action for the pair of (context, touchpad)
         *         will be cleared.
         *
         * @return A new set of configurations generated from the original configurations and necessary modifications
         *         to add the new (context, touchpad, action) configuration.
         */
        public static Set<Configuration> generate(DeviceConfigurations origin, GestureContext context,
                                                  Touchpad touchpad, @Nullable Action action) {
            DeviceConfigurations updatedConfiguration = updateConfigurations(origin, context, touchpad, action);
            return generateSet(updatedConfiguration);
        }

        /**
         * @return A new instance of DeviceConfigurations with the updates.
         */
        @NonNull
        private static DeviceConfigurations updateConfigurations(DeviceConfigurations origin, GestureContext context,
                                                                 Touchpad touchpad, @Nullable Action action) {
            // null -> clear the actions
            if (action == null) {
                DeviceConfigurations result = new DeviceConfigurations(origin);
                clearActions(result.configurations.computeIfAbsent(context, c -> new LinkedHashMap<>()), touchpad);
                return result;
            }

            if (context.equals(QTILGestureContexts.PASSTHROUGH_GLOBAL)) {
                return updateForGlobal(origin, touchpad, action);
            }
            else {
                // any other context
                return updateForNonGlobal(origin, context, touchpad, action);
            }
        }

        private static Set<Configuration> generateSet(DeviceConfigurations origin) {
            Set<Configuration> result = new LinkedHashSet<>();

            origin.configurations.forEach((context, actions) -> {
                if (actions != null && !actions.isEmpty()) {
                    actions.forEach((touchpad, action) -> {
                        if (action != null) {
                            result.add(new Configuration(touchpad, context, action));
                        }
                    });
                }
            });

            return result;
        }

        private static DeviceConfigurations updateForGlobal(DeviceConfigurations origin, Touchpad touchpad,
                                                            Action action) {
            DeviceConfigurations result = new DeviceConfigurations(origin);

            // clear the touchpad action for all contexts
            result.configurations.values().forEach(actions -> clearActions(actions, touchpad));

            // add new action
            addConfiguration(result, QTILGestureContexts.PASSTHROUGH_GLOBAL, touchpad, action);

            return result;
        }

        private static DeviceConfigurations updateForNonGlobal(DeviceConfigurations origin, GestureContext context,
                                                               Touchpad touchpad, Action action) {
            DeviceConfigurations result = new DeviceConfigurations(origin);

            // clear the touchpad action for global context and for current context
            clearActions(result.configurations.get(QTILGestureContexts.PASSTHROUGH_GLOBAL), touchpad);
            clearActions(result.configurations.get(context), touchpad);

            // add new action
            addConfiguration(result, context, touchpad, action);

            return result;
        }

        private static void clearActions(Map<Touchpad, Action> actions, Touchpad touchpad) {
            if (actions == null) {
                // nothing to clear
                return;
            }

            switch (touchpad) {
                case SINGLE:
                    // clear single value only
                    actions.put(Touchpad.SINGLE, null);
                    break;

                case RIGHT:
                    if (actions.get(Touchpad.LEFT) == null) {
                        // move action for BOTH to LEFT and clear BOTH
                        actions.put(Touchpad.LEFT, actions.get(Touchpad.BOTH));
                        actions.put(Touchpad.BOTH, null);
                    }
                    // clear action for touchpad
                    actions.put(Touchpad.RIGHT, null);
                    break;

                case LEFT:
                    if (actions.get(Touchpad.RIGHT) == null) {
                        // move action for BOTH to RIGHT and clear BOTH
                        actions.put(Touchpad.RIGHT, actions.get(Touchpad.BOTH));
                        actions.put(Touchpad.BOTH, null);
                    }
                    // clear action for touchpad
                    actions.put(Touchpad.LEFT, null);
                    break;

                case BOTH:
                    // left, right and both to all be cleared
                    actions.put(Touchpad.LEFT, null);
                    actions.put(Touchpad.RIGHT, null);
                    actions.put(Touchpad.BOTH, null);
                    break;
            }
        }

        private static void addConfiguration(DeviceConfigurations origin, GestureContext context, Touchpad touchpad,
                                             Action action) {
            // Context exists already
            if (origin.configurations.containsKey(context)) {
                Map<Touchpad, Action> actions =
                        origin.configurations.computeIfAbsent(context, c -> new LinkedHashMap<>());
                actions.put(touchpad, action);
                DeviceConfigurations.sortConfigurations(actions);
                return;
            }

            // Context doesn't exist, add with following priority
            // Priority VOICE > MUSIC > HANDSET
            // NOte: if the context is PASSTHROUGH_GLOBAL there should not be any other context
            if (QTILGestureContexts.PASSTHROUGH_GLOBAL.equals(context) ||
                    QTILGestureContexts.VOICE_IN_CALL_WITH_HELD.equals(context) ||
                    QTILGestureContexts.VOICE_IN_CALL.equals(context) ||
                    QTILGestureContexts.VOICE_INCOMING.equals(context) ||
                    QTILGestureContexts.VOICE_OUTGOING.equals(context)) {

                // Shallow copy of the current configurations
                LinkedHashMap<GestureContext, Map<Touchpad, Action>> currentConfig =
                        new LinkedHashMap<>(origin.configurations);

                // Clear the configurations so that we can re-enter them in the correct order.
                origin.configurations.clear();

                Map<Touchpad, Action> actions =
                        origin.configurations.computeIfAbsent(context, c -> new LinkedHashMap<>());
                actions.put(touchpad, action);

                origin.configurations.putAll(currentConfig);
            }
            else if (QTILGestureContexts.HANDSET_DISCONNECTED.equals(context) ||
                    QTILGestureContexts.HANDSET_CONNECTED.equals(context)) {

                // Handset goes at the end, so there's no need to copy, clear and put back.
                Map<Touchpad, Action> actions =
                        origin.configurations.computeIfAbsent(context, c -> new LinkedHashMap<>());
                actions.put(touchpad, action);
            }
            else {
                // Media goes in the middle.
                // Shallow copy of the current configurations
                LinkedHashMap<GestureContext, Map<Touchpad, Action>> currentConfig =
                        new LinkedHashMap<>(origin.configurations);

                // Clear the configurations so that we can re-enter them in the correct order.
                origin.configurations.clear();

                Iterator<Map.Entry<GestureContext, Map<Touchpad, Action>>> it = currentConfig.entrySet().iterator();

                boolean inserted = false;

                while (it.hasNext()) {
                    Map.Entry<GestureContext, Map<Touchpad, Action>> entry = it.next();

                    if (!entry.getKey().equals(QTILGestureContexts.HANDSET_CONNECTED) &&
                            !entry.getKey().equals(QTILGestureContexts.HANDSET_DISCONNECTED)) {
                        origin.configurations.put(entry.getKey(), entry.getValue());
                    }
                    else {
                        inserted = true;
                        // Put the action, and fill up the remaining.
                        Map<Touchpad, Action> actions =
                                origin.configurations.computeIfAbsent(context, c -> new LinkedHashMap<>());
                        actions.put(touchpad, action);
                        origin.configurations.put(entry.getKey(), entry.getValue());
                        it.forEachRemaining(a -> origin.configurations.put(a.getKey(), a.getValue()));
                        break;
                    }
                }

                if (!inserted) {
                    Map<Touchpad, Action> actions =
                            origin.configurations.computeIfAbsent(context, c -> new LinkedHashMap<>());
                    actions.put(touchpad, action);
                }

            }

            Map<Touchpad, Action> actions = origin.configurations.computeIfAbsent(context, c -> new LinkedHashMap<>());
            DeviceConfigurations.sortConfigurations(actions);
        }
    }

}
