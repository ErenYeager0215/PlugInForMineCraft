package plugin.enemydown;

import javax.xml.stream.events.EntityDeclaration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.enemydown.command.EnemyDownCommand;

public final class Main extends JavaPlugin{

  @Override
  public void onEnable() {
    EnemyDownCommand enemyDownCommand = new EnemyDownCommand(this);
    // Plugin startup logic
    Bukkit.getPluginManager().registerEvents(enemyDownCommand,this);
    getCommand("enemyDown").setExecutor(enemyDownCommand);
  }



}
