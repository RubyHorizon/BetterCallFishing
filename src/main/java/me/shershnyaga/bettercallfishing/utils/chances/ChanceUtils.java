package me.shershnyaga.bettercallfishing.utils.chances;

import lombok.NoArgsConstructor;

import java.util.*;


@NoArgsConstructor
public class ChanceUtils<T extends Chance> {

    private final static Random RANDOM = new Random();

    private float max = 1;

    public ChanceUtils(float max) {
        this.max = max;
    }

    @SafeVarargs
    public final Optional<T> getRandomItem(T... items) {
        float totalChance = 0;

        for (T item: items) {
            totalChance += item.getChance();
        }

        if (totalChance == 0) {
            return Optional.empty();
        }

        float randomVal = RANDOM.nextFloat() * totalChance;
        float cumulativeChance = 0;

        for (T item : items) {
            cumulativeChance += item.getChance();
            if (randomVal < cumulativeChance) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @SafeVarargs
    public final Optional<T> tryToGetRandomItem(T... items) {
        float totalAccessChance = 0;
        float totalFailleChance = 0;

        for (T item: items) {
            totalAccessChance += item.getChance();
            totalFailleChance += max - item.getChance();
        }

        if (totalAccessChance + totalAccessChance == 0) {
            return Optional.empty();
        }

        float randomVal = RANDOM.nextFloat() * (totalAccessChance + totalFailleChance);
        float cumulativeChance = 0;

        if (randomVal > totalAccessChance) {
            return Optional.empty();
        }

        for (T item : items) {
            cumulativeChance += item.getChance();
            if (randomVal < cumulativeChance) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }
}
