package util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FakePhonePriceDaily {

    private static final String[] NAMES = {
            "iPhone 14 Pro", "Samsung Galaxy S23", "Xiaomi Redmi Note 12", "Oppo Reno 10", "Vivo X80"
    };

    private static final String[] PRICES = {
            "20,000,000 VND", "18,000,000 VND", "10,000,000 VND", "12,000,000 VND", "15,000,000 VND"
    };

    private static final String[] PROCESSORS = {
            "A16 Bionic", "Snapdragon 8 Gen 2", "Dimensity 920", "Snapdragon 870", "Exynos 2200"
    };

    private static final String[] CAPACITIES = {
            "128GB", "256GB", "512GB", "1TB"
    };

    private static final String[] RAMS = {
            "6GB", "8GB", "12GB", "16GB"
    };

    private static final String[] SCREEN_SIZES = {
            "6.1 inches", "6.5 inches", "6.7 inches", "6.8 inches"
    };

    private static final String[] TRADEMARKS = {
            "Apple", "Samsung", "Xiaomi", "Oppo", "Vivo"
    };

    private static final String[] SOURCES = {
            "ShopDunk", "FPT Shop", "The Gioi Di Dong", "CellphoneS"
    };

    private static final String[] DATES = {
            "2024-11-01", "2024-11-02", "2024-11-03", "2024-11-04", "2024-11-05"
    };

    public static List<String[]> generateFakeData(int numberOfRecords) {
        List<String[]> data = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < numberOfRecords; i++) {
            String name = NAMES[random.nextInt(NAMES.length)];
            String price = PRICES[random.nextInt(PRICES.length)];
            String processor = PROCESSORS[random.nextInt(PROCESSORS.length)];
            String capacity = CAPACITIES[random.nextInt(CAPACITIES.length)];
            String ram = RAMS[random.nextInt(RAMS.length)];
            String screenSize = SCREEN_SIZES[random.nextInt(SCREEN_SIZES.length)];
            String trademark = TRADEMARKS[random.nextInt(TRADEMARKS.length)];
            String source = SOURCES[random.nextInt(SOURCES.length)];
            String createAt = DATES[random.nextInt(DATES.length)];

            data.add(new String[] { name, price, processor, capacity, ram, screenSize, trademark, source, createAt });
        }

        return data;
    }
}

