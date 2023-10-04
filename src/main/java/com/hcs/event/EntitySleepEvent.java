package com.hcs.event;

import com.hcs.status.accessor.StatAccessor;
import com.hcs.status.manager.PainManager;
import com.hcs.status.manager.StatusManager;
import com.hcs.status.manager.TemperatureManager;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.entity.player.HungerManager;

public class EntitySleepEvent {
    public static void init() {
        EntitySleepEvents.ALLOW_RESETTING_TIME.register(((player) -> {
            if (player != null && player.isSleeping()) {
                //Add: recovery depends on how long a player slept
                StatusManager statusManager = ((StatAccessor) player).getStatusManager();
                if (statusManager.getRecentSleepTicks() <= 0) {
                    statusManager.setRecentSleepTicks(600);
                    player.heal(player.getMaxHealth());
                    ((StatAccessor) player).getStaminaManager().reset();
                    ((StatAccessor) player).getThirstManager().addDirectly(-0.25);
                    ((StatAccessor) player).getSanityManager().reset();
                    HungerManager hungerManager = player.getHungerManager();
                    hungerManager.setExhaustion(0.0F);
                    hungerManager.setFoodLevel(Math.max(0, hungerManager.getFoodLevel() - 4));
                    TemperatureManager temperatureManager = ((StatAccessor) player).getTemperatureManager();
                    //Warm oneself by sleeping
                    if (temperatureManager.get() < 0.5) temperatureManager.set(0.5);
                    PainManager painManager = ((StatAccessor) player).getPainManager();
                    painManager.addRaw(-1);
                    painManager.setPainkillerApplied(0);
                }
            }
            return true;
        }));
    }
}
