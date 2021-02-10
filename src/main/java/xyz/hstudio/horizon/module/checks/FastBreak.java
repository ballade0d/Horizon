package xyz.hstudio.horizon.module.checks;

import net.minecraft.server.v1_8_R3.Enchantment;
import net.minecraft.server.v1_8_R3.MobEffect;
import net.minecraft.server.v1_8_R3.MobEffectList;
import net.minecraft.server.v1_8_R3.WorldSettings;
import xyz.hstudio.horizon.HPlayer;
import xyz.hstudio.horizon.api.enums.Detection;
import xyz.hstudio.horizon.event.Event;
import xyz.hstudio.horizon.event.inbound.ArmSwingEvent;
import xyz.hstudio.horizon.event.inbound.BlockDigEvent;
import xyz.hstudio.horizon.module.CheckBase;
import xyz.hstudio.horizon.wrapper.BlockWrapper;
import xyz.hstudio.horizon.wrapper.ItemWrapper;

public class FastBreak extends CheckBase {

    private BlockWrapper target;
    private float totalDamage;

    public FastBreak(HPlayer p) {
        super(p, 1, 200, 200);
    }

    @Override
    public void run(Event<?> event) {
        if (event instanceof ArmSwingEvent) {
            tickDig();
        } else if (event instanceof BlockDigEvent) {
            if (p.getMode() != WorldSettings.EnumGamemode.SURVIVAL) {
                return;
            }
            BlockDigEvent e = (BlockDigEvent) event;
            if (e.type == BlockDigEvent.DigType.START_DESTROY_BLOCK) {
                target = e.block;
                totalDamage = 0;
                tickDig();
            } else if (e.type == BlockDigEvent.DigType.STOP_DESTROY_BLOCK) {
                if (totalDamage == 0) {
                    punish(e, "NoSwing (tAxYy)", 1, Detection.NO_SWING, null);
                } else if (totalDamage < 1) {
                    double speedFactor = 1 / totalDamage;
                    double adder = Math.min((speedFactor - 1) * 10, 10);
                    punish(e, "FastBreak (7dvs8)", adder, Detection.FAST_BREAK, speedFactor + "x");
                }
                target = null;
                totalDamage = 0;
            }
        }
    }

    private void tickDig() {
        if (target == null || p.getMode() != WorldSettings.EnumGamemode.SURVIVAL) {
            return;
        }
        totalDamage += this.getDamage(target, p.inventory.hand());
    }

    private float getDamage(BlockWrapper block, ItemWrapper hand) {
        float hardness = block.hardness();
        float digSpeed = getDigSpeed(block, hand);
        return Math.max(!hand.canBreak(block) && !block.isAlwaysDestroyable() ?
                digSpeed / hardness / 100 : digSpeed / hardness / 30, 0);
    }

    private float getDigSpeed(BlockWrapper block, ItemWrapper hand) {
        float speed = 1;
        if (hand != null) {
            speed *= hand.breakSpeed(block);
        }
        if (speed > 1) {
            int level = hand.getEnchantmentLevel(Enchantment.DIG_SPEED);
            if (level > 0) {
                speed += level * level + 1;
            }
        }

        for (MobEffect effect : p.nms.getEffects()) {
            if (effect.getEffectId() == MobEffectList.FASTER_DIG.id) {
                speed *= 1 + (effect.getAmplifier() + 1) * 0.2f;
            } else if (effect.getEffectId() == MobEffectList.SLOWER_DIG.id) {
                float f1;
                switch (effect.getAmplifier()) {
                    case 0:
                        f1 = 0.3f;
                        break;
                    case 1:
                        f1 = 0.09f;
                        break;
                    case 2:
                        f1 = 0.0027f;
                        break;
                    case 3:
                    default:
                        f1 = 8.1E-4f;
                }
                speed *= f1;
            }
        }

        /*
        if (this.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(this)) {
            f /= 5f;
        }
        */

        if (!p.physics.onGround) {
            speed /= 5f;
        }

        return speed;
    }
}