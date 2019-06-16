package sh.okx.railswitch;

import com.google.common.base.CharMatcher;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import sh.okx.railswitch.database.RailSwitchDatabase;
import sh.okx.railswitch.listener.DectorRailActivateListener;
import vg.civcraft.mc.civmodcore.ACivMod;

public class RailSwitch extends ACivMod {
  private boolean timings;
  private RailSwitchDatabase database;

  @Override
  public void onEnable() {
    super.onEnable();
    saveDefaultConfig();
    loadDatabase();

    if (getConfig().getBoolean("timings")) {
      timings = true;
    }

    PluginManager pm = getServer().getPluginManager();

    pm.registerEvents(new DectorRailActivateListener(this), this);
    //pm.registerEvents(new DetectorRailUseListener(this), this);
    getCommand("setdestination").setExecutor(new SetDestinationCommand(this));
  }

  public RailSwitchDatabase getDatabase() {
    return database;
  }

  private void loadDatabase() {
    ConfigurationSection config = getConfig();

    String username = config.getString("mysql.username");
    String host = config.getString("mysql.host");
    String password = config.getString("mysql.password");
    String database = config.getString("mysql.database");
    String prefix = config.getString("mysql.prefix");
    int port = config.getInt("mysql.port");

    this.database = new RailSwitchDatabase(host, port, database, username, password, prefix, getLogger());
  }

  /**
   * make sure the message doesn't have any weirdness
   */
  public boolean isValidDestination(String message) {
    return message.length() <= 40
        && CharMatcher.inRange('0', '9')
        .or(CharMatcher.inRange('a', 'z'))
        .or(CharMatcher.inRange('A', 'Z'))
        .or(CharMatcher.anyOf("!\"#$%&'()*+,-./;:<=>?@[]\\^_`{|}~")).matchesAllOf(message);
  }

  public boolean isTimings() {
    return timings;
  }

  @Override
  protected String getPluginName() {
    return "RailSwitch";
  }
}
