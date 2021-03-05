package me.kingofmars4.trivia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Random;
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
	
	HashMap<String, String> questions = new HashMap<String, String>();
	
	@Override
	public void onEnable() {
		loadConfigs();
		databaseSetup();
		getQuestions();
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
				
				EconomyResponse r = econ.depositPlayer(e.getPlayer(), 5000);
	            if(r.transactionSuccess()) {
	                e.getPlayer().sendMessage(pluginPrefix+color("&5You were given &a%s and now have &a%d".replace("%s", econ.format(r.amount)).replace("%d", econ.format(r.balance))));
	            }
	            
				answer = null;
			}
			
		}
	}
            		
	public void getQuestions() {
    	try {
    		Statement statement = getConnection().createStatement();
			ResultSet result = statement.executeQuery("SELECT * FROM questions;");
	        
			if (result.next()) {
				 questions.put(result.getString("question"), result.getString("answer"));
            }
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
	}
	
    public void sendQuestion() {
        Object pickedQuest = questions.keySet().toArray()[new Random().nextInt(questions.keySet().toArray().length)];
        
        Bukkit.broadcastMessage(pluginPrefix+color("&5A new &fTRIVIA &5is now starting, answer the following question in order to win &a5000$&f!"));
        Bukkit.broadcastMessage(color("&e&lQuestion: &5&n"+pickedQuest.toString()));
        answer = questions.get(pickedQuest);
    }

	
	public boolean onCommand(CommandSender sender, Command cmd, String label,  String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("triviastart")) {
			if (sender.hasPermission("trivia.start")) {
				Bukkit.broadcastMessage(pluginPrefix+color("&e%p &5has started a new &fTrivia&5!".replace("%p", sender.getName())));
				sendQuestion();
			}
		} else if (cmd.getName().equalsIgnoreCase("triviarestart")) {
			if (sender.hasPermission("trivia.start")) {
				loadConfigs();
				questions.clear();
				getQuestions();
				sender.sendMessage(color("&aSuccefully reloaded trivia!"));
			}
		}
		
		return true;
	}
	
	public String host, database, username, password, port;
    private Connection connection;
    
    public Connection getConnection() {
    	return connection;
    }
    
    public void setConnection(Connection connection) {
    	this.connection = connection;
    }
    
    public void databaseSetup() {
    	host = "gajocraft.com";
        port = "3306";
        database = "trivia";
        username = "trivia";
        password = null;
        

		try {    
			if (getConnection() != null && !getConnection().isClosed()) {
			    return;
			}
			
			Class.forName("com.mysql.jdbc.Driver");
			
			setConnection(DriverManager.getConnection("jdbc:mysql://" + host+ ":" + port + "/" + database,username, password));
			
			Statement statement = getConnection().createStatement();
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS questions (" + 
					"    id INT NOT NULL," + 
					"    question VARCHAR(255) NOT NULL," + 
					"    answer VARCHAR(255) NOT NULL," +
					"    PRIMARY KEY (id)" + 
					");");
			
			
			
           getLogger().info("Suceffully connected to the MySQL database!");
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
		
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
}
