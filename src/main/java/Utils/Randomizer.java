package Utils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Randomizer {

    private final Random random;

    public Randomizer() {
        this.random = new Random();
    }

    public int[] getRandomIndices(int maxIndex, int numIndices) {
        int[] randomIndices = new int[numIndices];
        Set<Integer> indicesSet = new HashSet<>();
        while (indicesSet.size() < numIndices) {
            int index = random.nextInt(maxIndex);
            indicesSet.add(index);
        }

        int i = 0;
        for (int index : indicesSet) {
            randomIndices[i] = index;
            ++i;
        }

        return randomIndices;
    }

}
