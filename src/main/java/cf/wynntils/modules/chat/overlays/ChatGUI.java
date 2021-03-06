package cf.wynntils.modules.chat.overlays;

import cf.wynntils.modules.chat.enums.ChatTab;
import net.minecraft.client.gui.GuiChat;

import java.io.IOException;

public class ChatGUI extends GuiChat {
    
    public ChatGUI() {
        
    }
    
    public ChatGUI(String defaultInputText) {
        super(defaultInputText);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(ChatOverlay.getChat().getMouseOver() == 1) ChatOverlay.getChat().setCurrentTab(ChatTab.GLOBAL);
        else if(ChatOverlay.getChat().getMouseOver() == 2) ChatOverlay.getChat().setCurrentTab(ChatTab.GUILD);
        else if(ChatOverlay.getChat().getMouseOver() == 3) ChatOverlay.getChat().setCurrentTab(ChatTab.PARTY);


        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

}
