package ch.virt.tsvm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.util.ArrayList;

/**
 * Reads in the settings from the file
 * @author VirtCode
 * @version 1.0
 */
public class Data {
    private static final String TAG = "[Data] ";
    public static final String filename = "data.json";

    @Expose private String botToken;
    @Expose private String botUsername;

    @Expose private String restaurantSubDomain;
    @Expose private String hostString = "";

    @Expose private String[] menuNames = {};
    @Expose private int maxMenues = 10;

    @Expose private boolean printAdditional = false;
    @Expose private String[] menuBlacklist = {};

    @Expose private boolean useCustomStrings = false;

    @Expose private int schedulingHour = 8;

    @Expose private boolean enableReload = false;

    @Expose private boolean enableDatabase = false;
    @Expose private String databaseClientURI = "";

    private ArrayList<Long> subscriptions = new ArrayList<>();

    public String getBotToken() {
        return botToken;
    }

    public String getBotUsername() {
        return botUsername;
    }

    public String getRestaurantSubDomain() {
        return restaurantSubDomain;
    }

    public void addSubscription(long l){
        subscriptions.add(l);
    }

    public boolean isSubscribed(long l){
        return subscriptions.contains(l);
    }

    public void removeSubscription(long l){
        subscriptions.remove(l);
    }

    public Long[] getSubscriptions(){
        return subscriptions.toArray(new Long[0]);
    }

    public String getHostString() {
        return hostString;
    }

    public String getMenuName(int i) {
        if (i >= menuNames.length) return "Menu " + i;
        return menuNames[i];
    }

    public int getMaxMenues() {
        return maxMenues;
    }

    public boolean isPrintAdditional() {
        return printAdditional;
    }

    public String[] getMenuBlacklist() {
        return menuBlacklist;
    }

    public boolean isUseCustomStrings() {
        return useCustomStrings;
    }

    public int getSchedulingHour() {
        return schedulingHour;
    }

    public boolean isEnableReload() {
        return enableReload;
    }

    public boolean isEnableDatabase() {
        return enableDatabase;
    }

    public String getDatabaseClientURI() {
        return databaseClientURI;
    }

    /**
     * Reads it from a File
     * @return read Data
     */
    public static Data read(){
        File file = new File(filename);
        if (file.exists()){
            System.out.println(TAG + "Reading previous Saves");
            Gson gson = new Gson();
            try {
                FileReader fr = new FileReader(file);
                return gson.fromJson(fr, Data.class);
            } catch (FileNotFoundException e) {
                System.out.println(TAG + "Failed to read Previous saves");
                e.printStackTrace();
                return new Data();
            }
        }else {
            System.out.println(TAG + "Do previous saves Found");
            return new Data();
        }
    }

    /**
     * Saves it to a File
     */
    public void save(){
        File file = new File(filename);
        try {
            FileWriter fw = new FileWriter(file);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(this);
            fw.write(json);
            fw.close();
            System.out.println(TAG + "Saved new configs!");
        } catch (IOException e) {
            System.out.println(TAG + "Failed to save new Configs!");
            e.printStackTrace();
        }
    }
}
