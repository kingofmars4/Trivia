package me.kingofmars4.trivia;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class Main extends JavaPlugin implements Listener, CommandExecutor{
	
	public static String answer = null;
	private static Economy econ = null;
	
	@Override
	public void onEnable() {
		loadConfigs();
		setupEconomy();
		
		getServer().getPluginManager().registerEvents(this, this);
		
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
	        public void run() {
	            sendQuestion();
	        }
	    }, 0L, 20*60L*60L);
	}
	
	public void loadConfigs() {
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		getLogger().info("Configuration files succefully loaded.");
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if (answer != null) {
			if (e.getMessage().equalsIgnoreCase(answer)) {
				Bukkit.broadcastMessage(pluginPrefix+color("&e%p &5has succefully answered the question and won &a5000$&5!".replace("%p", e.getPlayer().getName())));
				Bukkit.broadcastMessage(pluginPrefix+color("&5The answer was: &e&n"+answer));
				answer = null;
			}
			EconomyResponse r = econ.depositPlayer(e.getPlayer(), 5000);
            if(r.transactionSuccess()) {
                e.getPlayer().sendMessage(pluginPrefix+color("&5You were given &a%s and now have &a%d".replace("%s", econ.format(r.amount)).replace("%d", econ.format(r.balance))));
            }
		}
	}
            		
	
	
    public void sendQuestion() {
    	ArrayList<String> questions = new ArrayList<String>();
        for (String key : getConfig().getConfigurationSection("Questions").getKeys(false)) { questions.add(key); }
        int chosen = random(0, questions.size()-1);
        
        Bukkit.broadcastMessage(pluginPrefix+color("&5A new &fTRIVIA &5is now starting, answer the following question in order to win &a5000$&f!"));
        Bukkit.broadcastMessage(color("&e&lQuestion: &5&n"+getConfig().getString("Questions."+questions.get(chosen)+".Quest")));
        answer = getConfig().getString("Questions."+questions.get(chosen)+".Answer");
    }

    
    public int random(int min, int max){
        return (int) ((Math.random()*((max-min)+1))+min);
    }
    
    public String pluginPrefix = color(getConfig().getString("Options.pluginPrefix"));
	
	
	public static String color(String s) {
		return s.replaceAll("&", "§");
	}
	
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
	
	public boolean onCommand(CommandSender sender, Command cmd, String label,  String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("triviastart")) {
			if (sender.hasPermission("trivia.start")) {
				Bukkit.broadcastMessage(pluginPrefix+color("&e%p &5has started a new &fTrivia&5!".replace("%p", sender.getName())));
				sendQuestion();
			}
		}
		
		return true;
	}
}
