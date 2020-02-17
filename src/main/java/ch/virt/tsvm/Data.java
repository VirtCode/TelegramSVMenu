package ch.virt.tsvm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.util.ArrayList;

/**
 * @author VirtCode
 * @version 1.0
 */
public class Data {
    private static final String TAG = "[Data] ";
    public static final String filename = "data.json";

    @SerializedName("botToken")
    private String token = "token";
    @SerializedName("botUsername")
    private String username = "username";
    @SerializedName("restaurantSubDomain")
    private String restaurantSubDomain = "sub-domain";

    @SerializedName("subscriptions")
    private ArrayList<Long> subscriptions;

    public Data(){
        subscriptions = new ArrayList<>();
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
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
