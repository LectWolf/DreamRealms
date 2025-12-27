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
 * Paper 服务端特有 API 工具类
 * 运行时检测是否在 Paper 服务端上运行，动态调用 Paper API
 */
public class PaperUtil {
    
    private static Boolean isPaper = null;
    
    /**
     * 检测是否在 Paper 服务端上运行
     */
    public static boolean isPaper() {
        if (isPaper == null) {
            try {
                Class.forName("io.papermc.paper.configuration.Configuration");
                isPaper = true;
            } catch (ClassNotFoundException e) {
                isPaper = false;
            }
        }
        return isPaper;
    }
    
    // ==================== ConsumableComponent ====================
    
    /**
     * 获取消耗时间（秒）
     * @return 消耗时间，如果不支持或未设置返回 null
     */
    @Nullable
    public static Float getConsumeSeconds(ItemStack item) {
        if (!isPaper()) return null;
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return null;
            
            Method hasConsumable = meta.getClass().getMethod("hasConsumable");
            if (!(Boolean) hasConsumable.invoke(meta)) return null;
            
            Method getConsumable = meta.getClass().getMethod("getConsumable");
            Object consumable = getConsumable.invoke(meta);
            if (consumable == null) return null;
            
            Method getConsumeSeconds = consumable.getClass().getMethod("getConsumeSeconds");
            return (Float) getConsumeSeconds.invoke(consumable);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 设置消耗时间（秒）
     */
    public static boolean setConsumeSeconds(ItemStack item, float seconds) {
        if (!isPaper()) return false;
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return false;
            
            Method getConsumable = meta.getClass().getMethod("getConsumable");
            Object consumable = getConsumable.invoke(meta);
            if (consumable == null) return false;
            
            Method setConsumeSeconds = consumable.getClass().getMethod("setConsumeSeconds", float.class);
            setConsumeSeconds.invoke(consumable, seconds);
            
            Method setConsumable = meta.getClass().getMethod("setConsumable", consumable.getClass().getInterfaces()[0]);
            setConsumable.invoke(meta, consumable);
            
            item.setItemMeta(meta);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // ==================== FoodComponent 扩展 ====================
    
    /**
     * 获取使用后转换的物品
     */
    @Nullable
    public static ItemStack getUsingConvertsTo(@Nullable FoodComponent food) {
        if (!isPaper() || food == null) return null;
        try {
            Method getUsingConvertsTo = food.getClass().getMethod("getUsingConvertsTo");
            return (ItemStack) getUsingConvertsTo.invoke(food);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 设置使用后转换的物品
     */
    public static boolean setUsingConvertsTo(@Nullable FoodComponent food, @Nullable ItemStack item) {
        if (!isPaper() || food == null) return false;
        try {
            Method setUsingConvertsTo = food.getClass().getMethod("setUsingConvertsTo", ItemStack.class);
            setUsingConvertsTo.invoke(food, item);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取食物效果数量
     */
    public static int getFoodEffectCount(@Nullable FoodComponent food) {
        if (!isPaper() || food == null) return 0;
        try {
            Method getEffects = food.getClass().getMethod("getEffects");
            List<?> effects = (List<?>) getEffects.invoke(food);
            return effects != null ? effects.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 获取食物效果列表（返回 FoodEffect 对象列表）
     */
    @SuppressWarnings("unchecked")
    public static List<Object> getFoodEffects(@Nullable FoodComponent food) {
        if (!isPaper() || food == null) return new ArrayList<>();
        try {
            Method getEffects = food.getClass().getMethod("getEffects");
            List<?> effects = (List<?>) getEffects.invoke(food);
            return effects != null ? new ArrayList<>((List<Object>) effects) : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    /**
     * 设置食物效果列表
     */
    public static boolean setFoodEffects(@Nullable FoodComponent food, List<Object> effects) {
        if (!isPaper() || food == null) return false;
        try {
            Method setEffects = food.getClass().getMethod("setEffects", List.class);
            setEffects.invoke(food, effects);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 添加食物效果
     * @return 新创建的 FoodEffect 对象，失败返回 null
     */
    @Nullable
    public static Object addFoodEffect(@Nullable FoodComponent food, PotionEffect effect, float probability) {
        if (!isPaper() || food == null) return null;
        try {
            Method addEffect = food.getClass().getMethod("addEffect", PotionEffect.class, float.class);
            return addEffect.invoke(food, effect, probability);
        } catch (Exception e) {
            return null;
        }
    }
    
    // ==================== FoodEffect 操作 ====================
    
    /**
     * 从 FoodEffect 获取 PotionEffect
     */
    @Nullable
    public static PotionEffect getEffectFromFoodEffect(Object foodEffect) {
        if (!isPaper() || foodEffect == null) return null;
        try {
            Method getEffect = foodEffect.getClass().getMethod("getEffect");
            return (PotionEffect) getEffect.invoke(foodEffect);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 从 FoodEffect 获取概率
     */
    public static float getProbabilityFromFoodEffect(Object foodEffect) {
        if (!isPaper() || foodEffect == null) return 0f;
        try {
            Method getProbability = foodEffect.getClass().getMethod("getProbability");
            return (Float) getProbability.invoke(foodEffect);
        } catch (Exception e) {
            return 0f;
        }
    }
}
