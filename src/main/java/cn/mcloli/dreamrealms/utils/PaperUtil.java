package cn.mcloli.dreamrealms.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Paper 1.21.8 Data Component API 工具类
 * 专门针对 1.21.2+ 的 Data Component API
 */
public class PaperUtil {
    
    private static Boolean isPaper = null;
    private static Class<?> dataComponentTypesClass = null;
    private static Class<?> dataComponentTypeClass = null;
    
    public static boolean isPaper() {
        if (isPaper == null) {
            try {
                Class.forName("io.papermc.paper.datacomponent.DataComponentTypes");
                isPaper = true;
            } catch (ClassNotFoundException e) {
                isPaper = false;
            }
        }
        return isPaper;
    }
    
    private static Class<?> getDataComponentTypesClass() throws ClassNotFoundException {
        if (dataComponentTypesClass == null) {
            dataComponentTypesClass = Class.forName("io.papermc.paper.datacomponent.DataComponentTypes");
        }
        return dataComponentTypesClass;
    }
    
    private static Class<?> getDataComponentTypeClass(Object componentType) {
        if (dataComponentTypeClass == null) {
            for (Class<?> iface : componentType.getClass().getInterfaces()) {
                if (iface.getSimpleName().contains("DataComponentType")) {
                    dataComponentTypeClass = iface;
                    break;
                }
            }
            if (dataComponentTypeClass == null) {
                dataComponentTypeClass = componentType.getClass().getInterfaces()[0];
            }
        }
        return dataComponentTypeClass;
    }
    
    // ==================== ConsumableComponent ====================
    
    @Nullable
    public static Float getConsumeSeconds(ItemStack item) {
        if (!isPaper()) return null;
        try {
            Object consumableType = getDataComponentTypesClass().getField("CONSUMABLE").get(null);
            Method getDataMethod = item.getClass().getMethod("getData", getDataComponentTypeClass(consumableType));
            Object consumable = getDataMethod.invoke(item, consumableType);
            if (consumable == null) return null;
            
            Method getConsumeSeconds = consumable.getClass().getMethod("consumeSeconds");
            return (Float) getConsumeSeconds.invoke(consumable);
        } catch (Exception e) {
            return null;
        }
    }
    
    public static boolean setConsumeSeconds(ItemStack item, float seconds) {
        if (!isPaper()) return false;
        try {
            Object consumableType = getDataComponentTypesClass().getField("CONSUMABLE").get(null);
            Class<?> typeClass = getDataComponentTypeClass(consumableType);
            
            Method getDataMethod = item.getClass().getMethod("getData", typeClass);
            Object existingConsumable = getDataMethod.invoke(item, consumableType);
            
            Class<?> consumableClass = Class.forName("io.papermc.paper.datacomponent.item.Consumable");
            Method builderMethod = consumableClass.getMethod("consumable");
            builderMethod.setAccessible(true);
            Object builder = builderMethod.invoke(null);
            
            Method consumeSecondsMethod = builder.getClass().getMethod("consumeSeconds", float.class);
            consumeSecondsMethod.setAccessible(true);
            consumeSecondsMethod.invoke(builder, seconds);
            
            if (existingConsumable != null) {
                copyConsumableProperties(existingConsumable, builder);
            }
            
            Method buildMethod = builder.getClass().getMethod("build");
            buildMethod.setAccessible(true);
            Object consumable = buildMethod.invoke(builder);
            
            Method setDataMethod = item.getClass().getMethod("setData", typeClass, Object.class);
            setDataMethod.setAccessible(true);
            setDataMethod.invoke(item, consumableType, consumable);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // ==================== USE_REMAINDER (使用后转换) ====================
    
    public static boolean setUsingConvertsToViaConsumable(ItemStack item, @Nullable ItemStack convertsTo) {
        if (!isPaper()) return false;
        try {
            Object useRemainderType = getDataComponentTypesClass().getField("USE_REMAINDER").get(null);
            Class<?> typeClass = getDataComponentTypeClass(useRemainderType);
            
            if (convertsTo != null) {
                Class<?> useRemainderClass = Class.forName("io.papermc.paper.datacomponent.item.UseRemainder");
                Method createMethod = useRemainderClass.getMethod("useRemainder", ItemStack.class);
                createMethod.setAccessible(true);
                Object useRemainder = createMethod.invoke(null, convertsTo);
                Method setDataMethod = item.getClass().getMethod("setData", typeClass, Object.class);
                setDataMethod.setAccessible(true);
                setDataMethod.invoke(item, useRemainderType, useRemainder);
            } else {
                Method unsetDataMethod = item.getClass().getMethod("unsetData", typeClass);
                unsetDataMethod.setAccessible(true);
                unsetDataMethod.invoke(item, useRemainderType);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Nullable
    public static ItemStack getUsingConvertsToViaConsumable(ItemStack item) {
        if (!isPaper()) return null;
        try {
            Object useRemainderType = getDataComponentTypesClass().getField("USE_REMAINDER").get(null);
            Method getDataMethod = item.getClass().getMethod("getData", getDataComponentTypeClass(useRemainderType));
            Object useRemainder = getDataMethod.invoke(item, useRemainderType);
            if (useRemainder == null) return null;
            
            // 查找返回 ItemStack 的方法
            for (Method m : useRemainder.getClass().getDeclaredMethods()) {
                if (m.getParameterCount() == 0 && ItemStack.class.isAssignableFrom(m.getReturnType())) {
                    m.setAccessible(true);
                    return (ItemStack) m.invoke(useRemainder);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    // ==================== FoodComponent (兼容旧 API) ====================
    
    @Nullable
    public static ItemStack getUsingConvertsTo(@Nullable FoodComponent food) {
        return null; // 1.21.8 不再使用 FoodComponent 存储此属性
    }
    
    public static boolean setUsingConvertsTo(@Nullable FoodComponent food, @Nullable ItemStack item) {
        return false; // 1.21.8 不再使用 FoodComponent 存储此属性
    }
    
    public static int getFoodEffectCount(@Nullable FoodComponent food) {
        return 0; // 1.21.8 效果在 ConsumableComponent 中
    }
    
    public static List<Object> getFoodEffects(@Nullable FoodComponent food) {
        return new ArrayList<>(); // 1.21.8 效果在 ConsumableComponent 中
    }
    
    public static boolean setFoodEffects(@Nullable FoodComponent food, List<Object> effects) {
        return false; // 1.21.8 效果在 ConsumableComponent 中
    }
    
    @Nullable
    public static Object addFoodEffect(@Nullable FoodComponent food, PotionEffect effect, float probability) {
        return null; // 1.21.8 使用 addFoodEffectViaConsumable
    }
    
    @Nullable
    public static PotionEffect getEffectFromFoodEffect(Object foodEffect) {
        return null; // 1.21.8 使用 getPotionEffectsFromConsumeEffect
    }
    
    public static float getProbabilityFromFoodEffect(Object foodEffect) {
        return 0f; // 1.21.8 使用 getProbabilityFromConsumeEffect
    }
    
    // ==================== ConsumableComponent 效果操作 ====================
    
    @SuppressWarnings("unchecked")
    public static List<Object> getConsumableEffects(ItemStack item) {
        if (!isPaper()) return new ArrayList<>();
        try {
            Object consumableType = getDataComponentTypesClass().getField("CONSUMABLE").get(null);
            Method getDataMethod = item.getClass().getMethod("getData", getDataComponentTypeClass(consumableType));
            Object consumable = getDataMethod.invoke(item, consumableType);
            if (consumable == null) return new ArrayList<>();
            
            // 尝试 consumeEffects 方法
            for (Method m : consumable.getClass().getDeclaredMethods()) {
                if (m.getName().equals("consumeEffects") && m.getParameterCount() == 0) {
                    m.setAccessible(true);
                    Object effects = m.invoke(consumable);
                    if (effects instanceof List) {
                        return new ArrayList<>((List<Object>) effects);
                    }
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static List<PotionEffect> getPotionEffectsFromConsumeEffect(Object consumeEffect) {
        if (consumeEffect == null) return new ArrayList<>();
        try {
            for (Method m : consumeEffect.getClass().getDeclaredMethods()) {
                if (m.getName().equals("effects") && m.getParameterCount() == 0) {
                    m.setAccessible(true);
                    Object effects = m.invoke(consumeEffect);
                    if (effects instanceof List) {
                        return new ArrayList<>((List<PotionEffect>) effects);
                    }
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public static float getProbabilityFromConsumeEffect(Object consumeEffect) {
        if (consumeEffect == null) return 1.0f;
        try {
            for (Method m : consumeEffect.getClass().getDeclaredMethods()) {
                if (m.getName().equals("probability") && m.getParameterCount() == 0) {
                    m.setAccessible(true);
                    return (Float) m.invoke(consumeEffect);
                }
            }
            return 1.0f;
        } catch (Exception e) {
            return 1.0f;
        }
    }
    
    public static boolean addFoodEffectViaConsumable(ItemStack item, PotionEffect effect, float probability) {
        if (!isPaper()) return false;
        try {
            Object consumableType = getDataComponentTypesClass().getField("CONSUMABLE").get(null);
            Class<?> typeClass = getDataComponentTypeClass(consumableType);
            
            Method getDataMethod = item.getClass().getMethod("getData", typeClass);
            Object existingConsumable = getDataMethod.invoke(item, consumableType);
            
            Class<?> consumableClass = Class.forName("io.papermc.paper.datacomponent.item.Consumable");
            Method builderMethod = consumableClass.getMethod("consumable");
            builderMethod.setAccessible(true);
            Object builder = builderMethod.invoke(null);
            
            if (existingConsumable != null) {
                copyConsumableProperties(existingConsumable, builder);
            }
            
            // 创建 ConsumeEffect
            Class<?> consumeEffectClass = Class.forName("io.papermc.paper.datacomponent.item.consumable.ConsumeEffect");
            Method applyEffectsMethod = consumeEffectClass.getMethod("applyStatusEffects", List.class, float.class);
            applyEffectsMethod.setAccessible(true);
            Object consumeEffect = applyEffectsMethod.invoke(null, List.of(effect), probability);
            
            // 添加效果
            for (Method m : builder.getClass().getDeclaredMethods()) {
                if (m.getName().equals("addEffect") && m.getParameterCount() == 1) {
                    m.setAccessible(true);
                    m.invoke(builder, consumeEffect);
                    break;
                }
            }
            
            Method buildMethod = builder.getClass().getMethod("build");
            buildMethod.setAccessible(true);
            Object consumable = buildMethod.invoke(builder);
            
            Method setDataMethod = item.getClass().getMethod("setData", typeClass, Object.class);
            setDataMethod.setAccessible(true);
            setDataMethod.invoke(item, consumableType, consumable);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean removeConsumableEffect(ItemStack item, int effectIndex) {
        if (!isPaper()) return false;
        try {
            List<Object> effects = getConsumableEffects(item);
            if (effectIndex < 0 || effectIndex >= effects.size()) return false;
            effects.remove(effectIndex);
            return setConsumableEffects(item, effects);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean setConsumableEffects(ItemStack item, List<Object> effects) {
        if (!isPaper()) return false;
        try {
            Object consumableType = getDataComponentTypesClass().getField("CONSUMABLE").get(null);
            Class<?> typeClass = getDataComponentTypeClass(consumableType);
            
            Method getDataMethod = item.getClass().getMethod("getData", typeClass);
            Object existingConsumable = getDataMethod.invoke(item, consumableType);
            
            Class<?> consumableClass = Class.forName("io.papermc.paper.datacomponent.item.Consumable");
            Method builderMethod = consumableClass.getMethod("consumable");
            builderMethod.setAccessible(true);
            Object builder = builderMethod.invoke(null);
            
            if (existingConsumable != null) {
                copyConsumablePropertiesWithoutEffects(existingConsumable, builder);
            }
            
            // 添加效果列表
            if (!effects.isEmpty()) {
                Method addEffects = builder.getClass().getMethod("addEffects", List.class);
                addEffects.setAccessible(true);
                addEffects.invoke(builder, effects);
            }
            
            Method buildMethod = builder.getClass().getMethod("build");
            buildMethod.setAccessible(true);
            Object consumable = buildMethod.invoke(builder);
            
            Method setDataMethod = item.getClass().getMethod("setData", typeClass, Object.class);
            setDataMethod.setAccessible(true);
            setDataMethod.invoke(item, consumableType, consumable);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    
    private static void copyConsumableProperties(Object existingConsumable, Object builder) {
        try {
            // 复制 consumeSeconds
            Method getConsumeSeconds = existingConsumable.getClass().getMethod("consumeSeconds");
            getConsumeSeconds.setAccessible(true);
            float seconds = (Float) getConsumeSeconds.invoke(existingConsumable);
            Method setConsumeSeconds = builder.getClass().getMethod("consumeSeconds", float.class);
            setConsumeSeconds.setAccessible(true);
            setConsumeSeconds.invoke(builder, seconds);
        } catch (Exception ignored) {}
        
        try {
            // 复制 animation
            Method getAnimation = existingConsumable.getClass().getMethod("animation");
            getAnimation.setAccessible(true);
            Object animation = getAnimation.invoke(existingConsumable);
            if (animation != null) {
                Class<?> animationClass = Class.forName("org.bukkit.inventory.ItemUseAnimation");
                Method setAnimation = builder.getClass().getMethod("animation", animationClass);
                setAnimation.setAccessible(true);
                setAnimation.invoke(builder, animation);
            }
        } catch (Exception ignored) {}
        
        try {
            // 复制 sound
            Method getSound = existingConsumable.getClass().getMethod("sound");
            getSound.setAccessible(true);
            Object sound = getSound.invoke(existingConsumable);
            if (sound != null) {
                Class<?> keyClass = Class.forName("net.kyori.adventure.key.Key");
                Method setSound = builder.getClass().getMethod("sound", keyClass);
                setSound.setAccessible(true);
                setSound.invoke(builder, sound);
            }
        } catch (Exception ignored) {}
        
        try {
            // 复制 hasConsumeParticles
            Method getParticles = existingConsumable.getClass().getMethod("hasConsumeParticles");
            getParticles.setAccessible(true);
            boolean particles = (Boolean) getParticles.invoke(existingConsumable);
            Method setParticles = builder.getClass().getMethod("hasConsumeParticles", boolean.class);
            setParticles.setAccessible(true);
            setParticles.invoke(builder, particles);
        } catch (Exception ignored) {}
        
        try {
            // 复制现有效果
            for (Method m : existingConsumable.getClass().getDeclaredMethods()) {
                if (m.getName().equals("consumeEffects") && m.getParameterCount() == 0) {
                    m.setAccessible(true);
                    Object effects = m.invoke(existingConsumable);
                    if (effects instanceof List && !((List<?>) effects).isEmpty()) {
                        Method addEffects = builder.getClass().getMethod("addEffects", List.class);
                        addEffects.setAccessible(true);
                        addEffects.invoke(builder, effects);
                    }
                    break;
                }
            }
        } catch (Exception ignored) {}
    }
    
    private static void copyConsumablePropertiesWithoutEffects(Object existingConsumable, Object builder) {
        try {
            Method getConsumeSeconds = existingConsumable.getClass().getMethod("consumeSeconds");
            getConsumeSeconds.setAccessible(true);
            float seconds = (Float) getConsumeSeconds.invoke(existingConsumable);
            Method setConsumeSeconds = builder.getClass().getMethod("consumeSeconds", float.class);
            setConsumeSeconds.setAccessible(true);
            setConsumeSeconds.invoke(builder, seconds);
        } catch (Exception ignored) {}
        
        try {
            Method getAnimation = existingConsumable.getClass().getMethod("animation");
            getAnimation.setAccessible(true);
            Object animation = getAnimation.invoke(existingConsumable);
            if (animation != null) {
                Class<?> animationClass = Class.forName("org.bukkit.inventory.ItemUseAnimation");
                Method setAnimation = builder.getClass().getMethod("animation", animationClass);
                setAnimation.setAccessible(true);
                setAnimation.invoke(builder, animation);
            }
        } catch (Exception ignored) {}
        
        try {
            Method getSound = existingConsumable.getClass().getMethod("sound");
            getSound.setAccessible(true);
            Object sound = getSound.invoke(existingConsumable);
            if (sound != null) {
                Class<?> keyClass = Class.forName("net.kyori.adventure.key.Key");
                Method setSound = builder.getClass().getMethod("sound", keyClass);
                setSound.setAccessible(true);
                setSound.invoke(builder, sound);
            }
        } catch (Exception ignored) {}
        
        try {
            Method getParticles = existingConsumable.getClass().getMethod("hasConsumeParticles");
            getParticles.setAccessible(true);
            boolean particles = (Boolean) getParticles.invoke(existingConsumable);
            Method setParticles = builder.getClass().getMethod("hasConsumeParticles", boolean.class);
            setParticles.setAccessible(true);
            setParticles.invoke(builder, particles);
        } catch (Exception ignored) {}
    }
    
    // ==================== DamageResistant 组件 ====================
    
    public static boolean hasDamageResistant(ItemStack item) {
        if (!isPaper()) return false;
        try {
            Object damageResistantType = getDataComponentTypesClass().getField("DAMAGE_RESISTANT").get(null);
            Method getDataMethod = item.getClass().getMethod("getData", getDataComponentTypeClass(damageResistantType));
            return getDataMethod.invoke(item, damageResistantType) != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean removeDamageResistant(ItemStack item) {
        if (!isPaper()) return false;
        try {
            Object damageResistantType = getDataComponentTypesClass().getField("DAMAGE_RESISTANT").get(null);
            item.getClass().getMethod("unsetData", getDataComponentTypeClass(damageResistantType)).invoke(item, damageResistantType);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Nullable
    public static String getDamageResistantTypes(ItemStack item) {
        if (!isPaper()) return null;
        try {
            Object damageResistantType = getDataComponentTypesClass().getField("DAMAGE_RESISTANT").get(null);
            Method getDataMethod = item.getClass().getMethod("getData", getDataComponentTypeClass(damageResistantType));
            Object damageResistant = getDataMethod.invoke(item, damageResistantType);
            if (damageResistant == null) return null;
            
            Method getTypes = damageResistant.getClass().getMethod("getTypes");
            Object types = getTypes.invoke(damageResistant);
            return types != null ? types.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    // ==================== 其他兼容方法 ====================
    
    public static boolean hasConsumable(ItemStack item) {
        if (!isPaper()) return false;
        try {
            Object consumableType = getDataComponentTypesClass().getField("CONSUMABLE").get(null);
            Method getDataMethod = item.getClass().getMethod("getData", getDataComponentTypeClass(consumableType));
            return getDataMethod.invoke(item, consumableType) != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean removeConsumable(ItemStack item) {
        if (!isPaper()) return false;
        try {
            Object consumableType = getDataComponentTypesClass().getField("CONSUMABLE").get(null);
            item.getClass().getMethod("unsetData", getDataComponentTypeClass(consumableType)).invoke(item, consumableType);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // ==================== ToolComponent ====================
    
    public static boolean hasTool(ItemStack item) {
        if (!isPaper()) return false;
        try {
            Object toolType = getDataComponentTypesClass().getField("TOOL").get(null);
            Method getDataMethod = item.getClass().getMethod("getData", getDataComponentTypeClass(toolType));
            return getDataMethod.invoke(item, toolType) != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Nullable
    public static Float getDefaultMiningSpeed(ItemStack item) {
        if (!isPaper()) return null;
        try {
            Object toolType = getDataComponentTypesClass().getField("TOOL").get(null);
            Method getDataMethod = item.getClass().getMethod("getData", getDataComponentTypeClass(toolType));
            Object tool = getDataMethod.invoke(item, toolType);
            if (tool == null) return null;
            
            Method getSpeed = tool.getClass().getMethod("defaultMiningSpeed");
            getSpeed.setAccessible(true);
            return (Float) getSpeed.invoke(tool);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Nullable
    public static Integer getDamagePerBlock(ItemStack item) {
        if (!isPaper()) return null;
        try {
            Object toolType = getDataComponentTypesClass().getField("TOOL").get(null);
            Method getDataMethod = item.getClass().getMethod("getData", getDataComponentTypeClass(toolType));
            Object tool = getDataMethod.invoke(item, toolType);
            if (tool == null) return null;
            
            Method getDamage = tool.getClass().getMethod("damagePerBlock");
            getDamage.setAccessible(true);
            return (Integer) getDamage.invoke(tool);
        } catch (Exception e) {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public static List<Object> getToolRules(ItemStack item) {
        if (!isPaper()) return new ArrayList<>();
        try {
            Object toolType = getDataComponentTypesClass().getField("TOOL").get(null);
            Method getDataMethod = item.getClass().getMethod("getData", getDataComponentTypeClass(toolType));
            Object tool = getDataMethod.invoke(item, toolType);
            if (tool == null) return new ArrayList<>();
            
            Method getRules = tool.getClass().getMethod("rules");
            getRules.setAccessible(true);
            Object rules = getRules.invoke(tool);
            if (rules instanceof List) {
                return new ArrayList<>((List<Object>) rules);
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public static boolean setToolProperties(ItemStack item, float defaultMiningSpeed, int damagePerBlock, List<Object> rules) {
        if (!isPaper()) return false;
        try {
            Object toolType = getDataComponentTypesClass().getField("TOOL").get(null);
            Class<?> typeClass = getDataComponentTypeClass(toolType);
            
            Class<?> toolClass = Class.forName("io.papermc.paper.datacomponent.item.Tool");
            Method builderMethod = toolClass.getMethod("tool");
            builderMethod.setAccessible(true);
            Object builder = builderMethod.invoke(null);
            
            Method setSpeed = builder.getClass().getMethod("defaultMiningSpeed", float.class);
            setSpeed.setAccessible(true);
            setSpeed.invoke(builder, defaultMiningSpeed);
            
            Method setDamage = builder.getClass().getMethod("damagePerBlock", int.class);
            setDamage.setAccessible(true);
            setDamage.invoke(builder, damagePerBlock);
            
            if (rules != null && !rules.isEmpty()) {
                Method addRules = builder.getClass().getMethod("addRules", List.class);
                addRules.setAccessible(true);
                addRules.invoke(builder, rules);
            }
            
            Method buildMethod = builder.getClass().getMethod("build");
            buildMethod.setAccessible(true);
            Object tool = buildMethod.invoke(builder);
            
            Method setDataMethod = item.getClass().getMethod("setData", typeClass, Object.class);
            setDataMethod.setAccessible(true);
            setDataMethod.invoke(item, toolType, tool);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static boolean removeTool(ItemStack item) {
        if (!isPaper()) return false;
        try {
            Object toolType = getDataComponentTypesClass().getField("TOOL").get(null);
            Method unsetDataMethod = item.getClass().getMethod("unsetData", getDataComponentTypeClass(toolType));
            unsetDataMethod.setAccessible(true);
            unsetDataMethod.invoke(item, toolType);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // ToolRule 辅助方法
    @Nullable
    public static Float getRuleSpeed(Object rule) {
        if (rule == null) return null;
        try {
            Method getSpeed = rule.getClass().getMethod("speed");
            getSpeed.setAccessible(true);
            return (Float) getSpeed.invoke(rule);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Nullable
    public static Boolean getRuleCorrectForDrops(Object rule) {
        if (rule == null) return null;
        try {
            Method getCorrect = rule.getClass().getMethod("correctForDrops");
            getCorrect.setAccessible(true);
            Object result = getCorrect.invoke(rule);
            // 返回的是 TriState
            if (result != null) {
                String name = result.toString();
                if (name.equals("TRUE")) return true;
                if (name.equals("FALSE")) return false;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    @Nullable
    public static String getRuleBlockTypes(Object rule) {
        if (rule == null) return null;
        try {
            Method getBlocks = rule.getClass().getMethod("blocks");
            getBlocks.setAccessible(true);
            Object blocks = getBlocks.invoke(rule);
            return blocks != null ? blocks.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}