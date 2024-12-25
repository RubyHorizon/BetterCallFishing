package me.shershnyaga.bettercallfishing;

import me.shershnyaga.bettercallfishing.config.parser.ItemStackParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


@DisplayName("Item Parser Test")
public class ItemParserConfigTest {

    @Test
    void testItemParserConfig() {
        ItemStackParser.Builder builder = ItemStackParser.Builder.builder();

        builder.setEnableChanceParse(true);
        builder.setEnableCountRangeParse(true);
        builder.setEnableEnchantmentsChanceParse(true);
        builder.setEnableEnchantmentsRangeParse(true);

        ItemStackParser itemParser = builder.build();


    }

}
