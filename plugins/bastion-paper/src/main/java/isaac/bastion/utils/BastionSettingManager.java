package isaac.bastion.utils;

import isaac.bastion.Bastion;
import isaac.bastion.BastionType;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.DisplayLocationSetting;

public class BastionSettingManager {

    private BooleanSetting bsiOverlay;
    private BooleanSetting showNoBastion;
    private DisplayLocationSetting bsiLocation;
    private BooleanSetting ignorePlacementWarnings;
    private BooleanSetting showBastionFields;

    public BastionSettingManager() {
        initSettings();
    }

    private void initSettings() {
        MenuSection menu = new MenuSection("Bastion", "All settings related to Bastion", PlayerSettingAPI.getMainMenu(),
            new ItemStack(BastionType.getBastionType(BastionType.getDefaultType()).getMaterial()));

        showBastionFields = new BooleanSetting(Bastion.getPlugin(), false, "Display Bastion fields", "bsvisual", "Shows the fields of bastion blocks in the world");
        bsiOverlay = new BooleanSetting(Bastion.getPlugin(), true, "Display Bastion Information", "bsiOverlay", "Shows if the block you're standing on is bastioned territory.");
        showNoBastion = new BooleanSetting(Bastion.getPlugin(), false, "Display if you are not in a bastion field", "showNoBastion", "If enabled, will display Bastion status, even if you are not currently in a bastion field");
        bsiLocation = new DisplayLocationSetting(Bastion.getPlugin(), DisplayLocationSetting.DisplayLocation.SIDEBAR, "BSI Location", "bsiLocation", new ItemStack(Material.ARROW), "BSI");
        ignorePlacementWarnings = new BooleanSetting(Bastion.getPlugin(), false, "Ignore placement warnings", "ignorePlacementWarnings", "Show placements warning in chat when placing in a bastion field");

        menu.registerToParentMenu();
        menu.registerSetting(showBastionFields);
        menu.registerSetting(bsiOverlay);
        menu.registerSetting(showNoBastion);
        menu.registerSetting(bsiLocation);
        menu.registerSetting(ignorePlacementWarnings);
    }

    public BooleanSetting getBsiOverlay() {
        return bsiOverlay;
    }

    public DisplayLocationSetting getBsiLocation() {
        return bsiLocation;
    }

    public boolean getShowNoBastion(UUID uuid) {
        return showNoBastion.getValue(uuid);
    }

    public boolean getIgnorePlacementMessages(UUID uuid) {
        return ignorePlacementWarnings.getValue(uuid);
    }

    public BooleanSetting getShowBastionFieldsSetting() {
        return this.showBastionFields;
    }

    public boolean showBastionFields(UUID uuid) {return showBastionFields.getValue(uuid);};

    public void toggleBastionFields(UUID uuid) {
        showBastionFields.setValue(uuid, !showBastionFields(uuid));
    }
}
