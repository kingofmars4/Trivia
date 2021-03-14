package me.kingofmars4.trivia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
	
	public Question question = null;
	private static Economy econ = null;
	
	ArrayList<Question> questions = new ArrayList<Question>();
	int currentQuestion = 0;
	
	@Override
	public void onEnable() {
		loadConfig();
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
	
	public void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		getLogger().info("Configuration file succefully loaded.");
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if (question != null) {
			if (e.getMessage().equalsIgnoreCase(question.getAnswer())) {
				Bukkit.broadcastMessage(pluginPrefix+color("&e%p &5has succefully answered the question and won &a%m points&5!".replace("%p", e.getPlayer().getName()).replace("%n", ""+question.getReward())));
				Bukkit.broadcastMessage(pluginPrefix+color("&5The answer was: &e&n"+question.getAnswer()));
				
				@SuppressWarnings("deprecation")
				EconomyResponse r = econ.depositPlayer(e.getPlayer().getName(), question.getReward());
				if (r.transactionSuccess()) {
					e.getPlayer().sendMessage(pluginPrefix+color("&5You were given &e%s points&5 and now have &e%d".replace("%s", ""+question.getReward()).replaceAll("%d", ""+econ.getBalance(e.getPlayer()))));
				}
	            
	            question = null;
			}
			
		}
	}
            		
	public void getQuestions() {
    	try {
    		Statement statement = getConnection().createStatement();
			ResultSet result = statement.executeQuery("SELECT * FROM questions;");
	        
			if (result.next()) {
				if (!result.getString("question").endsWith("?") || !Character.isUpperCase(result.getString("question").charAt(0))) {
					getLogger().info("Question: "+result.getString(result.getString("question")));
					System.out.println("Could not be loaded: Must end with a '?' and start with a capital letter!");
					return;
				}

				 questions.add(new Question(result.getString("question"), result.getString("answer"), result.getDouble("reward")));
            }
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
	}
	
    public void sendQuestion() {
        question = questions.get(currentQuestion);
        if (currentQuestion+1 > questions.size()-1) {
        	currentQuestion = 0;
        } else {
        	currentQuestion++;
        }
        
        Bukkit.broadcastMessage(pluginPrefix+color("&5A new &fTRIVIA &5is now starting, answer the following question in order to win &e%n points&f!".replace("%n", question.getReward()+"")));
        Bukkit.broadcastMessage(color("&e&lQuestion: &5&n"+question.getQuestion()));

        getQuestions();
    }

	
	public boolean onCommand(CommandSender sender, Command cmd, String label,  String[] args) {
		
		if (cmd.getName().equalsIgnoreCase("triviastart")) {
			if (sender.hasPermission("trivia.start")) {
				Bukkit.broadcastMessage(pluginPrefix+color("&e%p &5has started a new &fTrivia&5!".replace("%p", sender.getName())));
				sendQuestion();
			}
		} else if (cmd.getName().equalsIgnoreCase("triviarestart")) {
			if (sender.hasPermission("trivia.start")) {
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
    	host = getConfig().getString("MySQL.host");
        port = getConfig().getString("MySQL.port");
        database = getConfig().getString("MySQL.database");
        username = getConfig().getString("MySQL.username");
        if (getConfig().getBoolean("MySQL.usingPassword")) {
        	password = getConfig().getString("MySQL.password");
        } else { password = null; }
        

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
					"    reward DOUBLE NOT NULL," +
					"    difficulty INT NOT NULL DEFAULT 3," +
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
    
    public String pluginPrefix = color(getConfig().getString("pluginPrefix"));
	
	
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
