package io.github.hsyyid.payday.utils;

import io.github.hsyyid.payday.PayDay;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static TimeUnit getTimeUnit() {
        ConfigurationNode valueNode = PayDay.config.getNode((Object[]) ("timeunit").split("\\."));

        if (valueNode.getValue() != null) {
            String value = valueNode.getString();

            if (value.toLowerCase().equals("days")) {
                return TimeUnit.DAYS;
            } else if (value.toLowerCase().equals("hours")) {
                return TimeUnit.HOURS;
            } else if (value.toLowerCase().equals("minutes")) {
                return TimeUnit.MINUTES;
            } else if (value.toLowerCase().equals("seconds")) {
                return TimeUnit.SECONDS;
            } else if (value.toLowerCase().equals("microseconds")) {
                return TimeUnit.MICROSECONDS;
            } else if (value.toLowerCase().equals("milliseconds")) {
                return TimeUnit.MILLISECONDS;
            } else if (value.toLowerCase().equals("nanoseconds")) {
                return TimeUnit.NANOSECONDS;
            } else {
                System.out.println("Error! TimeUnit not recognized: " + value);
                return TimeUnit.HOURS;
            }
        } else {
            Utils.setConfig("timeunit", "Hours");
            return TimeUnit.HOURS;
        }
    }

    public static int getTimeAmount() {
        ConfigurationNode valueNode = PayDay.config.getNode((Object[]) ("timeamount").split("\\."));

        try {
            String value = valueNode.getString();
            return Integer.parseInt(value);
        } catch (RuntimeException e) {
            Utils.setConfig("timeamount", "1");
            return 1;
        }
    }

    public static boolean getJoinPay() {
        ConfigurationNode valueNode = PayDay.config.getNode((Object[]) ("payonjoin").split("\\."));

        try {
            String value = valueNode.getString();
            if (value == null) {
                Utils.setConfig("payonjoin", Boolean.toString(true));
                return true;
            } else {
                return Boolean.parseBoolean(value);
            }
        } catch (RuntimeException e) {
            Utils.setConfig("payonjoin", Boolean.toString(true));
            return true;
        }
    }

    public static boolean enableAfkPay() {
        ConfigurationNode valueNode = PayDay.config.getNode((Object[]) ("enableafkpay").split("\\."));

        try {
            String value = valueNode.getString();
            if (value == null) {
                Utils.setConfig("enableafkpay", Boolean.toString(true));
                return true;
            } else {
                return Boolean.parseBoolean(value);
            }
        } catch (RuntimeException e) {
            Utils.setConfig("enableafkpay", Boolean.toString(true));
            return true;
        }
    }

    public static Map<String, BigDecimal> getPaymentAmounts() {
        Map<String, BigDecimal> payments = new HashMap<>();
        Map<Object, ? extends ConfigurationNode> children = PayDay.config.getNode("payamounts").getChildrenMap();

        for (ConfigurationNode child : children.values()) {
            if (!child.getNode("permission").isVirtual() && !child.getNode("amount").isVirtual()) {
                payments.put(child.getNode("permission").getString(), BigDecimal.valueOf(child.getNode("amount").getDouble()));
            }
        }

        return payments;
    }

    public static Text getFirstJoinMessage(BigDecimal amount) {
        ConfigurationNode valueNode = PayDay.config.getNode((Object[]) ("joinmessage").split("\\."));

        String defaultMessage = "&6[PayDay]: &7Welcome to the server! Here is {amount} {label}! Enjoy!";

        EconomyService economyService = PayDay.economyService;
        String label = economyService.getDefaultCurrency().getDisplayName().toPlain();
        if (amount.compareTo(BigDecimal.ONE) != 0) {
            label = economyService.getDefaultCurrency().getPluralDisplayName().toPlain();
        }

        try {
            String value = valueNode.getString();
            if (value == null) {
                Utils.setConfig("joinmessage", defaultMessage);
            } else {
                return TextSerializers.FORMATTING_CODE.deserialize(value.replaceAll("\\{amount\\}", amount.toString()).replaceAll("\\{label\\}",
                        label));
            }
        } catch (RuntimeException e) {
            Utils.setConfig("joinmessage", defaultMessage);
        }

        return TextSerializers.FORMATTING_CODE.deserialize(defaultMessage.replaceAll("\\{amount\\}", amount.toString()).replaceAll("\\{label\\}",
                label));
    }

    public static Text getSalaryMessage(BigDecimal amount) {
        ConfigurationNode valueNode = PayDay.config.getNode((Object[]) ("salarymessage").split("\\."));

        String defaultMessage = "&6[PayDay]: &7It's PayDay! Here is your salary of {amount} {label}! Enjoy!";

        EconomyService economyService = PayDay.economyService;
        String label = economyService.getDefaultCurrency().getDisplayName().toPlain();
        if (amount.compareTo(BigDecimal.ONE) != 0) {
            label = economyService.getDefaultCurrency().getPluralDisplayName().toPlain();
        }

        try {
            String value = valueNode.getString();
            if (value == null) {
                Utils.setConfig("salarymessage", defaultMessage);
            } else {
                return TextSerializers.FORMATTING_CODE.deserialize(value.replaceAll("\\{amount\\}", amount.toString()).replaceAll("\\{label\\}",
                        label));
            }
        } catch (RuntimeException e) {
            Utils.setConfig("salarymessage", defaultMessage);
        }

        return TextSerializers.FORMATTING_CODE.deserialize(defaultMessage.replaceAll("\\{amount\\}", amount.toString()).replaceAll("\\{label\\}",
                label));
    }

    public static void setConfig(String key, String value) {
        ConfigurationLoader<CommentedConfigurationNode> configManager = PayDay.getConfigManager();
        PayDay.config.getNode(key).setValue(value);

        try {
            configManager.save(PayDay.config);
            configManager.load();
        } catch (IOException e) {
            System.out.println("Failed to save " + key + "!");
        }
    }
}
