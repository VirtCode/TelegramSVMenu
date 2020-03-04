package ch.virt.tsvm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.*;

/**
 * This class contains and reads in the Strings from the strings file
 * @author VirtCode
 * @version 1.0
 */
public class Translation {

    private static final String TAG = "[Strings] ";
    private static final String filename = "strings.json";

    static final String newLine = "\n";
    @Expose private String tabSpaces = "   ";
    @Expose private String commandIndicator = "/";

    @Expose private String helpInstructor = "Unknown command! Use /help for help!";

    @Expose private String helpHeader = "The Following commands are Supported:";
    @Expose private String helpStart = "Display this Message.";
    @Expose private String helpHelp = "Display the start Message.";
    @Expose private String helpSubscribe = "Subscribe to the Menu notifications";
    @Expose private String helpUnsubscribe = "Unsubscribe from the Menu notifications.";
    @Expose private String helpMenu = "Get the current Menu.";
    @Expose private String helpTomorrow = "Get the Menu of tomorrow.";
    @Expose private String helpInfo = "Get info about the Restaurant the bot is linked to.";
    @Expose private String helpHost = "Get info about who has set up and is maintaining the bot.";
    @Expose private String helpAbout = "Get info about this bot and its creators.";

    @Expose private String startMessage = "Hey there! Use /subscribe to subscribe to the Menu notifications. For other Stuff use /help!";

    @Expose private String subscribeAlready = "You are already subscribed to the Menu notifications. Use  /unsubscribe to unsubscribe from the notifications.";
    @Expose private String subscribeSuccess = "You are now successfully subscribed to the Menu notifications.";

    @Expose private String unsubscribeAlready = "You are not subscribed to the Menu notifications. Use /subscribe to subscribe to it.";
    @Expose private String unsubscribeSuccess = "You are not subscribed to the Menu notifications anymore.";

    @Expose private String menuOffline = "It seems like the menuplan is offline!\nYou can try again by calling /menu!";
    @Expose private String menuDateFormat = "dd.MM.yy";
    @Expose private String menuAdditional = "Additional: ";
    @Expose private String menuVegetarian = "Is Vegetarian: ";

    @Expose private String hostString = "This bot has been set up and is maintained by:\n";

    @Expose private String infoRestaurant = "This bot is linked to";
    @Expose private String infoDomain = "You find the real menuplan at:";
    @Expose private String infoOffline = "Sorry but the restaurant page seems to be offline!";

    public String getStartMessage() {
        return startMessage;
    }

    public String getSubscribeAlready() {
        return subscribeAlready;
    }

    public String getSubscribeSuccess() {
        return subscribeSuccess;
    }

    public String getUnsubscribeAlready() {
        return unsubscribeAlready;
    }

    public String getUnsubscribeSuccess() {
        return unsubscribeSuccess;
    }

    public String getMenuOffline() {
        return menuOffline;
    }

    public String getMenuDateFormat() {
        return menuDateFormat;
    }

    public String getMenuAdditional() {
        return menuAdditional;
    }

    public String getMenuVegetarian() {
        return menuVegetarian;
    }

    public String getHostString() {
        return hostString;
    }

    public String getInfoRestaurant() {
        return infoRestaurant;
    }

    public String getInfoDomain() {
        return infoDomain;
    }

    public String getInfoOffline() {
        return infoOffline;
    }

    public String getTabSpaces() {
        return tabSpaces;
    }

    public String getCommandIndicator() {
        return commandIndicator;
    }

    public String getHelpInstructor() {
        return helpInstructor;
    }

    public String getHelp(){
        StringBuilder sb = new StringBuilder();
        sb.append(helpHeader);
        appendHelpEntry(sb, "help", helpHelp);
        appendHelpEntry(sb, "start", helpStart);
        appendHelpEntry(sb, "subscribe", helpSubscribe);
        appendHelpEntry(sb, "unsubscribe", helpUnsubscribe);
        appendHelpEntry(sb, "menu", helpMenu);
        appendHelpEntry(sb, "tomorrow", helpTomorrow);
        appendHelpEntry(sb, "info", helpInfo);
        appendHelpEntry(sb, "host", helpHost);
        appendHelpEntry(sb, "about", helpAbout);
        return sb.toString();
    }
    private void appendHelpEntry(StringBuilder sb, String command, String instructions){
        sb.append(newLine);
        sb.append(tabSpaces);
        sb.append(commandIndicator);
        sb.append(command);
        sb.append(newLine);
        sb.append(tabSpaces);
        sb.append(tabSpaces);
        sb.append(instructions);
    }

    /**
     * Reads it from the file
     * @return read Translations
     */
    public static Translation read(){
        File file = new File(filename);
        if (file.exists()){
            System.out.println(TAG + "Reading Strings");
            Gson gson = new Gson();
            try {
                FileReader fr = new FileReader(file);
                return gson.fromJson(fr, Translation.class);
            } catch (FileNotFoundException e) {
                System.out.println(TAG + "Failed to read Strings, using default Strings");
                e.printStackTrace();
                return new Translation();
            }
        }else {
            System.out.println(TAG + "No custom Strings found, using default Strings");
            return new Translation();
        }
    }

    /**
     * Saves it to the file
     * (For test purposes only)
     */
    private void save(){
        File file = new File(filename);
        try {
            FileWriter fw = new FileWriter(file);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(this);
            fw.write(json);
            fw.close();
            System.out.println(TAG + "Saved new strings!");
        } catch (IOException e) {
            System.out.println(TAG + "Failed to save new strings!");
            e.printStackTrace();
        }
    }

    /**
     * Test method to create a new strings.json file with the default strings.
     * @param args plain java stuff
     */
    public static void main(String[] args) {
        new Translation().save();
    }

}
