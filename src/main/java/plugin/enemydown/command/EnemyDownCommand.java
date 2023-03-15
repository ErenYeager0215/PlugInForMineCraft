package plugin.enemydown.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.SplittableRandom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import plugin.enemydown.Main;
import plugin.enemydown.data.PlayerScore;

public class EnemyDownCommand implements CommandExecutor, Listener {

  private Main main;
  private int  gameTime = 20;

  private List<PlayerScore> playerScoreList = new ArrayList<>();
  private World world;

  public EnemyDownCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if(sender instanceof Player player){
      PlayerScore nowPlayer = getPlayerScore(player);
      nowPlayer.setGameTime(20);


      World world = player.getWorld();

      initPlayerStatus(player);

      Bukkit.getScheduler().runTaskTimer(main,Runnable ->{
        if(nowPlayer.getGameTime() <= 0){
          Runnable.cancel();
          player.sendTitle("ゲームが終了しました",nowPlayer.getPlayerName() + "は"+nowPlayer.getScore()+"スコアでした",0,30,0);
          nowPlayer.setScore(0);
          List<Entity> nearbyEnemies = player.getNearbyEntities(50,0,50);
          for(Entity enemy:nearbyEnemies){
            switch (enemy.getType()){
              case ZOMBIE,SKELETON,WITCH -> enemy.remove();
            }
          }
          return;
        }
        world.spawnEntity(getEnemySpawnLocation(player, world),getEnemy());
        nowPlayer.setGameTime(nowPlayer.getGameTime() -5);
      },0,5*20);
    }
    return false;
  }

  /**
   * 現在実行しているプレイヤーのスコア情報を取得する
   * @param player コマンドを実行したプレイヤーの情報
   * @return 現在実行しているプレイヤーのスコア情報
   */

  private PlayerScore getPlayerScore(Player player) {
    if(playerScoreList.isEmpty()){
      return addNewPlayer(player);
    }else{
      for(PlayerScore playerScore : playerScoreList){
        if(!playerScore.getPlayerName().equals(player.getName())){
          return addNewPlayer(player);
        }else{
          return playerScore;
        }
      }
    }
    return null;
  }


  /**
   * Entityが死亡したら発生するイベント
   * @param e Entityが死亡したら発生するイベントのオブジェクト
   */

  @EventHandler
  public void onEnemyDeath(EntityDeathEvent e){
    LivingEntity enemy = e.getEntity();
    Player player = enemy.getKiller();
    if (Objects.isNull(player) || playerScoreList.isEmpty()) {
      return;
    }

    for(PlayerScore playerScore : playerScoreList){
      if(playerScore.getPlayerName().equals(player.getName())){
        int point = switch (enemy.getType()){
          case ZOMBIE -> 10;
          case SKELETON,WITCH -> 20;
          default -> 0;
        };

        playerScore.setScore(playerScore.getScore()+point);
        player.sendMessage("敵をたおしたよ!現在のスコアは"+playerScore.getScore()+"だよ！");
      }
    }
  }

  /**
   * 新規のプレイヤー情報をリストに追加する
   * @param player　コマンドを実行したプレイヤー情報
   * @return 新規プレイヤー
   */

  private PlayerScore addNewPlayer(Player player) {
    PlayerScore newPlayer = new PlayerScore();
    newPlayer.setPlayerName(player.getName());
    playerScoreList.add(newPlayer);
    return newPlayer;
  }

  /**
   * ゲームを始める前にプレイヤーの状態を初期化する
   * 体力と空腹度を最大にして、装備はネザーライト一式になる
   * @param player　コマンドを実行したプレイヤー
   */
  private static void initPlayerStatus(Player player) {
    player.setHealth(20);
    player.setFoodLevel(20);

    PlayerInventory Inventory = player.getInventory();
    Inventory.setHelmet(new ItemStack(Material.DIAMOND_HELMET));
    Inventory.setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
    Inventory.setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
    Inventory.setBoots(new ItemStack(Material.DIAMOND_BOOTS));
    Inventory.setItemInMainHand(new ItemStack(Material.DIAMOND_SWORD));
  }


  /**
   * 敵の出現エリアを取得する
   * 出現するエリアはX軸とZ軸はプレイヤーの場所からプラスしてランダムで、-10~9の値に設定されます
   * Y軸はプレイヤーと同じ位置になります。
   *
   * @param player　コマンドを実行したプレイヤー
   * @param world　コマンドを実行したプレイヤーが所属するワールド
   * @return 敵の出現場所
   */

  private  Location getEnemySpawnLocation(Player player, World world) {
    Location playerLocation = player.getLocation();
//      ランダム値を生成 今回は0~19の20個を生成
//      マイナス10しているのは座標がマイナスになるとプレイヤーの背後から現れることもできるため
    int randomX = new SplittableRandom().nextInt(20) - 10;
    int randomZ = new SplittableRandom().nextInt(20) - 10;
    double x = playerLocation.getX() + randomX;
    double y = playerLocation.getY();
    double z = playerLocation.getZ() + randomZ;
    return new Location(world,x,y,z);
  }

  /**
   * ランダムで敵を抽選してその結果の敵を取得する
   * @return 敵
   */
  private  EntityType getEnemy() {
    List<EntityType> enemyList = List.of(EntityType.ZOMBIE,EntityType.SKELETON,EntityType.WITCH);
    int randam = new SplittableRandom().nextInt(enemyList.size());
    EntityType enemy = enemyList.get(randam);
    return enemy;
  }
}
