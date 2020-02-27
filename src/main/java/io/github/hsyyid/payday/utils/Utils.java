package io.github.hsyyid.payday.utils;

import io.github.hsyyid.payday.PayDay;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Utils {

    private static TimeUnit timeUnit;
    private static int timeAmount;
    private static boolean joinPay;
    private static boolean enableAfkPay;
    private static Map<String, BigDecimal> paymentAmounts;
    private static String firstJoinMessage;
    private static String salaryMessage;
    public static Map<String, List<UUID>> paidPlayersMap;

    public static void loadConfig() {
        ConfigurationLoader confManager = PayDay.getConfigManager();

        try {
            PayDay.config = confManager.load();
        } catch (IOException exception) {
            PayDay.getLogger().error("Error! The configuration could not be loaded! Resetting configuration.", exception);
            PayDay.config = confManager.createEmptyNode();
        }

        loadTimeUnit();
        loadTimeAmount();
        loadJoinPay();
        loadEnableAfkPay();
        loadPaymentAmounts();
        loadFirstJoinMessage();
        loadSalaryMessage();

        try {
            confManager.save(PayDay.config);
        } catch (IOException exception) {
            PayDay.getLogger().warn("Error! The configuration could not be saved!", exception);
        }
    }

    private static void loadTimeUnit() {
        ConfigurationNode valueNode = PayDay.config.getNode("timeunit");

        String defaultValue = "hours";

        String value = loadConfigurationNode(valueNode, defaultValue).get();

        switch (value.toLowerCase()) {
            case "days":
            case "day":
                timeUnit = TimeUnit.DAYS;
                break;
            case "hours":
            case "hour":
                timeUnit = TimeUnit.HOURS;
                break;
            case "minutes":
            case "minute":
                timeUnit = TimeUnit.MINUTES;
                break;
            case "seconds":
            case "second":
                timeUnit = TimeUnit.SECONDS;
                break;
            default:
                PayDay.getLogger().error("Error! timeunit not recognized: \"" + value + "\". Expected \"days\", \"hours\", \"minutes\", or \"seconds\". Defaulting to \"" + defaultValue + "\".");
                timeUnit = TimeUnit.HOURS;
        }
    }

    public static TimeUnit getTimeUnit() {
        return timeUnit;
    }

    private static void loadTimeAmount() {
        ConfigurationNode valueNode = PayDay.config.getNode("timeamount");

        int defaultValue = 1;

        // Convert the setting to an int if it's currently a String
        String valueStr = valueNode.getString();
        if(valueStr != null && valueStr.matches("(0|[1-9]\\d*)")) {
            try {
                timeAmount = Integer.parseInt(valueStr);
                valueNode.setValue(timeAmount);
                return;
            }
            catch(NumberFormatException exception) {
                PayDay.getLogger().error("Error! timeamount not recognized: \"" + valueStr + "\". Expected a positive integer. Defaulting to " + defaultValue + ".", exception);
            }
        }

        timeAmount = loadConfigurationNode(valueNode, defaultValue).get();
    }

    public static int getTimeAmount() {
        return timeAmount;
    }

    private static void loadJoinPay() {
        ConfigurationNode valueNode = PayDay.config.getNode("payonjoin");

        String defaultValue = "true";

        String value = loadConfigurationNode(valueNode, defaultValue).get();
        if(!value.equals("true") && !value.equals("false")) {
            PayDay.getLogger().error("Error! payonjoin not recognized: \"" + value + "\". Expected \"true\" or \"false\". Defaulting to \"" + defaultValue + "\".");
            joinPay = Boolean.parseBoolean(defaultValue);
        }
        else {
            // Returns false if !value.equals("true")
            joinPay = Boolean.parseBoolean(value);
        }
    }

    public static boolean getJoinPay() {
        return joinPay;
    }

    private static void loadEnableAfkPay() {
        ConfigurationNode valueNode = PayDay.config.getNode("enableafkpay");

        String defaultValue = "true";

        String value = loadConfigurationNode(valueNode, defaultValue).get();
        if(!value.equals("true") && !value.equals("false")) {
            PayDay.getLogger().error("Error! payonjoin not recognized: \"" + value + "\". Expected \"true\" or \"false\". Defaulting to \"" + defaultValue + "\".");
            enableAfkPay = Boolean.parseBoolean(defaultValue);
        }
        else {
            // Returns false if !value.equals("true")
            enableAfkPay = Boolean.parseBoolean(value);
        }
    }

    public static boolean enableAfkPay() {
        return enableAfkPay;
    }

    private static void loadPaymentAmounts() {
        paymentAmounts = new HashMap<>();
        paidPlayersMap = new HashMap<>();

        ConfigurationNode node = PayDay.config.getNode("payamounts");
        Map<Object, ? extends ConfigurationNode> children = node.getChildrenMap();

        for (ConfigurationNode child : children.values()) {
            if (!child.getNode("permission").isVirtual() && !child.getNode("amount").isVirtual()) {
                String permission = child.getNode("permission").getString();
                paymentAmounts.put(permission, BigDecimal.valueOf(child.getNode("amount").getDouble()));
                paidPlayersMap.put(permission, new ArrayList<>());
            }
        }
    }

    public static Map<String, BigDecimal> getPaymentAmounts() {
        return paymentAmounts;
    }

    private static void loadFirstJoinMessage() {
        ConfigurationNode valueNode = PayDay.config.getNode("joinmessage");

        firstJoinMessage = loadConfigurationNode(valueNode, "&6[PayDay]: &7Welcome to the server! Here is {amount} {label}! Enjoy!").get();
    }

    public static Text getFirstJoinMessage(BigDecimal amount) {
        String label = getCurrencyLabel(amount);

        return TextSerializers.FORMATTING_CODE.deserialize(firstJoinMessage.replaceAll("\\{amount}", amount.toString())
                .replaceAll("\\{label}",label));
    }

    private static void loadSalaryMessage() {
        ConfigurationNode valueNode = PayDay.config.getNode("salarymessage");

        salaryMessage = loadConfigurationNode(valueNode, "&6[PayDay]: &7It's PayDay! Here is your salary of {amount} {label}! Enjoy!").get();
    }

    public static Text getSalaryMessage(BigDecimal amount) {
        String label = getCurrencyLabel(amount);

        return TextSerializers.FORMATTING_CODE.deserialize(salaryMessage.replaceAll("\\{amount}", amount.toString())
                .replaceAll("\\{label}", label));
    }

    private static String getCurrencyLabel(BigDecimal amount) {
        EconomyService economyService = PayDay.getEconomyService();

        if (amount.compareTo(BigDecimal.ONE) != 0) {
            return economyService.getDefaultCurrency().getDisplayName().toPlain();
        }
        else {
            return economyService.getDefaultCurrency().getPluralDisplayName().toPlain();
        }
    }

    private static <T> Optional<T> loadConfigurationNode(ConfigurationNode node, @Nullable T defaultValue) {
        return loadConfigurationNode(node, null, defaultValue);
    }

    // Loads a ConfigurationNode's comment and value, making changes as necessary
    // defaultValue can be null if the node only contains other nodes. in this case, null will be returned as well.
    // comment can be null if the node is not a CommentedConfigurationNode or doesn't need its comment set.
    @SuppressWarnings("unchecked")
    private static <T> Optional<T> loadConfigurationNode(ConfigurationNode node, @Nullable String comment, @Nullable T defaultValue) {
        T nodeValue = null;

        if(comment != null && node instanceof CommentedConfigurationNode) {
            CommentedConfigurationNode cNode = (CommentedConfigurationNode) node;
            // If the node doesn't exist, or its comment is unset or incorrect, set the comment
            if (node.isVirtual() || !cNode.getComment().isPresent() || !cNode.getComment().get().equals(comment)) {
                cNode.setComment(comment);
            }
        }

        if(defaultValue != null) {
            // Get the current value, or null if it's unset
            Object nodeValueObj = node.getValue();

            // If the value has never been set, set it silently
            if (node.isVirtual() || nodeValueObj == null) {
                node.setValue(defaultValue);
                nodeValue = defaultValue;
            }
            else {
                // Ensure the type of the node's value is the same as defaultValue's type
                if (defaultValue.getClass().isInstance(nodeValueObj)) {
                    nodeValue = (T) nodeValueObj;
                }
                else {
                    PayDay.getLogger().warn("Configuration value " + node.getKey() + " of type " + nodeValueObj.getClass().getName() + " was found when it should have been " + defaultValue.getClass().getName());
                }
            }

        }

        return Optional.ofNullable(nodeValue);
    }

    public static void warnIfMissingAfkService() {
        Utils.warnIfMissingAfkService(null);
    }

    public static void warnIfMissingAfkService(@Nullable CommandSource src) {
        if(PayDay.getAfkService() == null && enableAfkPay()) {
            String warning1 = "Error! AFK payments are disabled in the config, but Nucleus' AFK service wasn't found!";
            String warning2 = "PayDay will be unable to determine whether players are AFK.";

            if (src == null) {
                PayDay.getLogger().error(warning1);
                PayDay.getLogger().error(warning2);
            } else {
                src.sendMessage(Text.of(warning1));
                src.sendMessage(Text.of(warning2));
            }
        }
    }
}
