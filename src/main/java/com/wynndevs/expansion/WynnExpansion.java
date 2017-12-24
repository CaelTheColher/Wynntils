package com.wynndevs.expansion;

import com.wynndevs.ModCore;
import com.wynndevs.core.enums.ModuleResult;
import com.wynndevs.core.input.KeyBindings;
import com.wynndevs.expansion.Experience.*;
import com.wynndevs.expansion.ItemGuide.ItemGuideGUI;
import com.wynndevs.expansion.Misc.*;
import com.wynndevs.expansion.Options.Config;
import com.wynndevs.expansion.Options.GuiSHCMWynnOptions;
import com.wynndevs.expansion.PartyFriendsGuild.*;
import com.wynndevs.expansion.QuestBook.GuiQuestBook;
import com.wynndevs.expansion.QuestBook.QuestBook;
import com.wynndevs.expansion.QuestBook.QuestTrackingUI;
import com.wynndevs.expansion.Update.Update;
import com.wynndevs.expansion.Update.UpdateUI;
import com.wynndevs.expansion.WebAPI.TerritoryUI;
import com.wynndevs.expansion.WebAPI.WebAPI;
import com.wynndevs.expansion.WebAPI.WynnTerritory;
import com.wynndevs.expansion.WynnSound.GuiWynnSound;
import com.wynndevs.expansion.WynnSound.WynnSound;
import com.wynndevs.expansion.WynnSound.WynnSounds;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.NameFormat;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.ArrayList;
import java.util.List;

public class WynnExpansion {
	
	public static boolean TerritoryNews = false;
	public static boolean UseLegacyExperience = false;
	public static boolean OptimiseWar = false;
	public static boolean InfoOverrideFind = false;
	public static Delay UpdateCheckDelay = new Delay(3630.0f, true);
	public static boolean PotionShiftOff = false;
	public static boolean PotionEnabled = false;
	public static boolean DisableFOV = false;
	public static boolean ShowTPS = false;
	public static boolean HeaderVersion = false;
	
	private static boolean ProcessChatQue = false;
	private static Delay ProcessChatQueTimer = new Delay(10.0f, false);
	public static List<String> ChatQue = new ArrayList<String>();
	
	private static Delay TickRateLimiter = new Delay(0.04f, true);
	
	
	public static ModuleResult initModule(FMLPreInitializationEvent event) {
		
		ExpReference.VERSION = event.getModMetadata().version;
		
		Update.checkLatest();
		
		ClientCommandHandler.instance.registerCommand(new CordsCommand());
		ClientCommandHandler.instance.registerCommand(new InfoCommand());
		ClientCommandHandler.instance.registerCommand(new TerritoryCommand());
		
		ExpReference.ConsoleOut("The current version is: v" + ExpReference.VERSION);
		if (Update.newUpdate) {
			ExpReference.ConsoleOut("Update v" + Update.latest + " is Available!");
		}
		
		Experience.PreInit();
		LegacyExperience.PreInit();
		
		WynnSound.wynnSounds = new WynnSounds();
		
		// GENERATE DATA LISTS \\
		//WebAPI.StartAPI();
		//////			   \\\\\\
		
		MinecraftForge.EVENT_BUS.register(new WynnExpansion());
		MinecraftForge.EVENT_BUS.register(new StickyItems());
		
		return ModuleResult.SUCCESS;
	}
	
	public static void init(FMLInitializationEvent event) {
		Config.Refresh();
	}
	
	@SubscribeEvent
	public void eventHandler(InputEvent.KeyInputEvent event) {
		if (ExpReference.inGame()) {
			if (KeyBindings.OPEN_QUEST_BOOK.isPressed()){
				ModCore.mc().displayGuiScreen(new GuiQuestBook());
				//ModCore.mc().displayGuiScreen(new GuiSHCMWynnOptions());
			}
		}
		if (KeyBindings.OPEN_WYNN_SOUND.isPressed()) {
			if (ExpReference.inServer()) {
				ModCore.mc().displayGuiScreen(new GuiWynnSound());
			}
		}
		if (ExpReference.inGame()) {
			if (KeyBindings.SPELL_1.isPressed()) {
				SpellCasting.AddSpell("RLR");
			}
			if (KeyBindings.SPELL_2.isPressed()) {
				SpellCasting.AddSpell("RRR");
			}
			if (KeyBindings.SPELL_3.isPressed()) {
				SpellCasting.AddSpell("RLL");
			}
			if (KeyBindings.SPELL_4.isPressed()) {
				SpellCasting.AddSpell("RRL");
			}
			if (KeyBindings.OPEN_PLAYER_MENU.isPressed()){
				ModCore.mc().displayGuiScreen(new PlayerHomeMenu());
			}
			if (KeyBindings.OPEN_ITEM_GUIDE.isPressed()){
				ModCore.mc().displayGuiScreen(new ItemGuideGUI());
			}
		}
	}
	
	@SubscribeEvent
	public void eventHandler(RenderGameOverlayEvent.Text event) {
		if (ExpReference.inServer()) {
			if (TerritoryNews) {
				new TerritoryUI(ModCore.mc());
			}
			if (UpdateCheckDelay.Passed()){
				Update.checkLatest();
				UpdateUI.Show = true;
			}else if (Update.newUpdate) {
				new UpdateUI(ModCore.mc());
			}
			new GuildAttackTimer(ModCore.mc());
		}
		if (ExpReference.inGame()) {
			new PartyHUD(ModCore.mc());
			new SkillpointUI(ModCore.mc());
			new CompassUI(ModCore.mc());
			if (PotionEnabled) new PotionDisplay(ModCore.mc());
			new SpellCastingUI(ModCore.mc());
			if (UseLegacyExperience) {
				new LegacyExperienceUI(ModCore.mc());
			} else {
				new ExperienceUI(ModCore.mc());
			}
			if (ExpReference.inWar()) new WarTimer(ModCore.mc());
			if (QuestBook.selectedQuestTracking && !ExpReference.inWar()) {
				new QuestTrackingUI(ModCore.mc());
			}
			if (((WorldItemName.AnnounceMythic && MythicAnnounce.MythicAnnounce) || (WorldItemName.AnnounceLegendary && MythicAnnounce.LegendaryAnnounce)) && !ExpReference.inWar()){
				new MythicAnnounce(ModCore.mc());
			}
			new Announcments(ModCore.mc());
		}
	}
	
	@SubscribeEvent
	public void eventHandler(GuiOpenEvent event) {
		Gui gui = null;
		try {
			gui = event.getGui();
		} catch (Exception ignored) {
		}
		if (ExpReference.inGame()) {
			if (gui != null && gui instanceof GuiScreenBook) {
				if (!ExpReference.inWar() && !ExpReference.inNether()){
					QuestBook.ReloadBook();
					event.setGui(new GuiQuestBook());
				}else{
					event.setGui(new GuiSHCMWynnOptions());
				}
			}
		}
	}
	
	@SubscribeEvent//(priority = EventPriority.HIGHEST)
	public void eventHandler(TickEvent.ClientTickEvent event) {
		try{
			if (ProcessChatQue && ShowTPS && ExpReference.inGame() && ModCore.mc().world != null) {
				TpsMonitor.SetFooter();
			}
		}catch(Exception ignore) {}
		
		if (TickRateLimiter.Passed()){
			ExpReference.UpdateStatus();
			if (ExpReference.inGame()) {
				if (!ExpReference.inWar()) {
					WorldItemName.t(ModCore.mc());
					DailyChestReminder.CheckDailyChest();
					GuildAttack.GuildChatWarCoords();
					SoulpointTime.SoulpointPrintTime();
					PartyHUD.CapturePlayerEntities(ModCore.mc());
					if (WarTimer.TimeStamp != 0L) WarTimer.TimeStamp = 0L;
				}
				if (!ExpReference.inNether()) PlayerGlow.PlayerGlows(ModCore.mc());
				if (event.phase == TickEvent.Phase.START) {
					if (!UseLegacyExperience) {
						Experience.getAddedAmounts();
						if (((ExperienceUI.EnableScrollingSidebar && (ExperienceUI.ExpFlowShowNames || ExperienceUI.ExpFlowShowLevel)) || ExperienceUI.KillStreak) && !ExpReference.inWar()) {
							ExperienceAdvanced.EntangleMobs(ModCore.mc());
							ExperienceAdvanced.GatherExp(ModCore.mc());
						}
					}
				}
				if (HeaderVersion) {Update.SetHeader();}
				
				if (!ProcessChatQue && ProcessChatQueTimer.Passed()){
					ProcessChatQue = true;
				}else if (ProcessChatQue && !ChatQue.isEmpty()){
					if (!ChatQue.get(0).startsWith("--OPENGUI--")){
						ModCore.mc().player.sendChatMessage(ChatQue.get(0));
					}else{
						if (ChatQue.get(0).endsWith("PlayerInfoMenu")){
							ModCore.mc().displayGuiScreen(new PlayerInfoMenu());
						}
					}
					ChatQue.remove(0);
				}
			}else{
				ProcessChatQueTimer.Reset();
				if (ProcessChatQue) {
					ProcessChatQue = false;
				}
			}
			WynnSound.Update();
			if (event.phase == TickEvent.Phase.START) {
				SpellCasting.CastSpell();
			}
			if ((ExpReference.inServer()) && PlayerHomeMenu.RefreshTimer.Passed() && ModCore.mc().inGameHasFocus && PlayerHomeMenu.PlayersLoaded){
				PlayerHomeMenu.ClearLists();
			}
			if (ExpReference.inServer() && !ExpReference.inGame()) {
				Config.PostUpdateMessages();
			}
			// Game Rest Stuff
			if (ExpReference.inServer() && TestPlayerLevel0()){
				SkillpointUI.Skillpoints = 0;
				PotionDisplay.Effects.clear();
				ExpReference.CleanScoreboards();
				WarTimer.TimeStamp = 0L;
				TpsMonitor.TpsTimes.clear();
				ModCore.mc().ingameGUI.getTabList().resetFooterHeader();
			}
		}
	}
	
	private static boolean PlayerLevelCheckDone = false;
	private static boolean TestPlayerLevel0() {
		try {
			if (ModCore.mc().player.experienceLevel < 1) {
				if (!PlayerLevelCheckDone) {
					PlayerLevelCheckDone = true;
					return true;
				}
				return false;
			}else{
				if (PlayerLevelCheckDone) {
					PlayerLevelCheckDone = false;
				}
				return false;
			}
		}catch (Exception ignore){
		}
		return false;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void eventHandlerReformatChat(ClientChatReceivedEvent event){
		ChatReformater.Reformat(event);
	}
	
	@SubscribeEvent
	public void eventHandler(ClientChatReceivedEvent event){
		
		if (event.getType() == 1) {
			String msg = event.getMessage().getUnformattedText();
			String msgRaw = event.getMessage().getFormattedText();
			
			if (!event.isCanceled()) event.setCanceled(GuildAttack.ChatHandler(msg));
			if (!event.isCanceled()) event.setCanceled(PlayerGlow.ChatHandler(msg, msgRaw));
			if (!event.isCanceled()) PotionDisplay.UsePotion(msg);
			if (!event.isCanceled()) ExperienceAdvancedChat.ChatHandler(msg, msgRaw);
			if (!event.isCanceled()) DailyChestReminder.ChatHandler(msg);
			if (!event.isCanceled()) ChatManipulator.ChatHandler(event);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void eventHandlerTimeStamp(ClientChatReceivedEvent event){
		if (!event.isCanceled() && event.getType() == 1) ChatTimeStamp.TimeStamp(event);
	}
	
	@SubscribeEvent
	public void eventHandler(ConfigChangedEvent event) {
		Config.Refresh();
	}
	
	@SubscribeEvent
	public void eventHandler(EntityJoinWorldEvent event){
		if (OptimiseWar && ExpReference.inWar()) {
			if (WarOptimiser.OptimiseWar(event.getEntity()))
				event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public void eventHandler(ItemTossEvent event) {
		event.getEntityItem().addTag("ITEMTHROWN");
		event.getEntity().addTag("ITEMTHROWN");
	}
	
	@SubscribeEvent
	public void eventHandler(ClientChatEvent event){
		String msg = event.getMessage();
		if (InfoOverrideFind && ExpReference.inServer() && msg.startsWith("/find")){
			event.setMessage(msg.replace("/find", "/info"));
		}
	}
	
	@SubscribeEvent
	public void eventHandler(GuiScreenEvent.InitGuiEvent.Post event){
		if (ExpReference.inGame()){
			if ((event.getGui() instanceof GuiContainer) == false || event.getGui().mc == null || event.getGui().mc.player == null){
				return;
			}
			SkillpointUI.SkillpointUpdate(event);
			DailyChestReminder.DailyChestReseter(event);
			StickyItems.BankCheck(event);
		}
	}
	
	@SubscribeEvent
	public void eventHandler(NameFormat event){
		PlayerGlow.UpdateUsername(event);
	}
	
	@SubscribeEvent
	public void eventHandler(FMLNetworkEvent.ClientConnectedToServerEvent event){
		if (WebAPI.Done) WebAPI.StartAPI();
		
		if (StickyItems.SavedDropKeyCode == 0){
			ModCore.mc().gameSettings.keyBindDrop.setToDefault();
			StickyItems.SavedDropKeyCode = ModCore.mc().gameSettings.keyBindDrop.getKeyCode();
		}else{
			ModCore.mc().gameSettings.keyBindDrop.setKeyCode(StickyItems.SavedDropKeyCode);
		}
	}
	
	@SubscribeEvent
	public void eventHandler(FMLNetworkEvent.ClientDisconnectionFromServerEvent event){
		WebAPI.Running = false;
		while (!WebAPI.Done){
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignore) {}
		}
		
		GuildAttack.CurrentTerritory = new WynnTerritory();
		GuildAttackTimer.Timer = -1;
		SkillpointUI.Skillpoints = 0;
		PotionDisplay.Effects.clear();
	}
	
	@SubscribeEvent
	public void eventHandler(GuiScreenEvent.PotionShiftEvent event){
		if (PotionShiftOff) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public void eventHandler(FOVUpdateEvent event) {
		if (DisableFOV) {
			event.setNewfov(1.0F);
		}
	}
}