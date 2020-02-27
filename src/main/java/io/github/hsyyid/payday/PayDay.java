package io.github.hsyyid.payday;

import com.google.inject.Inject;
import io.github.hsyyid.payday.commands.PayDayCommand;
import io.github.hsyyid.payday.utils.Utils;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.service.NucleusAFKService;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

@Plugin(id = "payday", name = "PayDay", version = "1.5.0", description = "Pay your players as they play.", dependencies = {@Dependency(
        id = "nucleus", optional = true)})
public class PayDay {

    public static ConfigurationNode config;
    private static EconomyService economyService;
    private static PayDay instance;
    private Task task;
    private boolean functional = false;
    private static boolean afkServicePresent = false;
    private static NucleusAFKService afkService;

    @Inject private Logger logger;

    @Inject private PluginContainer container;

    @Inject @DefaultConfig(sharedRoot = true) private ConfigurationLoader<CommentedConfigurationNode> confManager;

    @Listener
    public void onGameInit(GameInitializationEvent event) {
        instance = this;
        logger.info("PayDay loading...");

        Utils.loadConfig();

        Sponge.getCommandManager().register(this, PayDayCommand.getCommandSpec(), "payday");

        createTask();

        logger.info("-----------------------------");
        logger.info("PayDay was made by HassanS6000!");
        logger.info("Patched to APIv5 by Kostronor from the Minecolonies team!");
        logger.info("Further updated by Flibio!");
        logger.info("Improved by Brycey92!");
        logger.info("Please post all errors on the Sponge Thread or on GitHub!");
        logger.info("Have fun, and enjoy! :D");
        logger.info("-----------------------------");
        logger.info("PayDay loaded!");
    }

    private void createTask() {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        task = taskBuilder.execute(task ->
        {
            // Reset who's been paid, for all payment amounts
            for(List<UUID> array: Utils.paidPlayersMap.values()){
                array.clear();
            }

            for (Player player : Sponge.getServer().getOnlinePlayers()) {
                // Check if the player is afk
                if (Utils.enableAfkPay() || ! (afkServicePresent && afkService.isAFK(player))) {
                    for (Entry<String, BigDecimal> entry : Utils.getPaymentAmounts().entrySet()) {
                        String key = entry.getKey();

                        if ((key.equals("*") || player.hasPermission(key)) && !Utils.paidPlayersMap.get(key).contains(player.getUniqueId())) {
                            BigDecimal pay = entry.getValue();
                            player.sendMessage(Utils.getSalaryMessage(pay));
                            UniqueAccount uniqueAccount = economyService.getOrCreateAccount(player.getUniqueId()).get();
                            uniqueAccount.deposit(economyService.getDefaultCurrency(), pay, Cause.of(EventContext.empty(), container));
                            Utils.paidPlayersMap.get(key).add(player.getUniqueId());
                        }
                    }
                }
            }
        }).interval(Utils.getTimeAmount(), Utils.getTimeUnit()).name("PayDay - Pay").submit(this);
    }

    @Listener
    public void onGamePostInit(GamePostInitializationEvent event) {
        Optional<EconomyService> econService = Sponge.getServiceManager().provide(EconomyService.class);
        if (econService.isPresent()) {
            economyService = econService.get();
            functional = true;
        }
        else {
            logger.error("Error! There is no Economy plugin found on this server, PayDay will not work!");
            task.cancel();
            functional = false;
            return;
        }

        Optional<NucleusAFKService> afkServiceOptional = NucleusAPI.getAFKService();
        if (afkServiceOptional.isPresent()) {
            afkService = afkServiceOptional.get();
            afkServicePresent = true;
        }
        else {
            Utils.warnIfMissingAfkService();
            afkServicePresent = false;
        }
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        reload();
    }

    public static void reload() {
        Utils.loadConfig();

        instance.task.cancel();
        instance.createTask();

        if(!afkServicePresent) {
            Utils.warnIfMissingAfkService();
        }
        instance.logger.info("Reloaded config!");
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        if (functional && Utils.getJoinPay()) {
            Player player = event.getTargetEntity();

            for (Entry<String, BigDecimal> entry : Utils.getPaymentAmounts().entrySet()) {
                String key = entry.getKey();

                if ((key.equals("*") || player.hasPermission(key)) && !Utils.paidPlayersMap.get(key).contains(player.getUniqueId())) {
                    BigDecimal pay = entry.getValue();
                    player.sendMessage(Utils.getFirstJoinMessage(pay));
                    UniqueAccount uniqueAccount = economyService.getOrCreateAccount(player.getUniqueId()).get();
                    uniqueAccount.deposit(economyService.getDefaultCurrency(), pay, Cause.of(EventContext.empty(), container));
                    Utils.paidPlayersMap.get(key).add(player.getUniqueId());
                }
            }
        }
    }

    public static PayDay getInstance() { return instance; }

    public static ConfigurationLoader<CommentedConfigurationNode> getConfigManager() { return instance.confManager; }

    public static Logger getLogger() { return instance.logger; }

    public static EconomyService getEconomyService() { return economyService; }

    public static NucleusAFKService getAfkService() { return afkService; }
}
