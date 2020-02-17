package ch.virt.tsvm;

import ch.virt.svrestaurant.api.Restaurant;
import ch.virt.svrestaurant.api.menu.Menu;
import ch.virt.svrestaurant.api.menu.MenuDay;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author VirtCode
 * @version 1.0
 */
public class Main extends TelegramLongPollingBot {

    public static final String PREFIX = "[MenuBot] ";

    private String currentMenuString;

    private Timer timer;
    private Restaurant restaurant;
    private Data data;

    public static void main(String[] args) {
        create();
    }

    public static Main create(){
        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();
        Main bot = new Main();

        try {
            botsApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            System.err.println(PREFIX + "Failed to register Telegram Bot");
        }

        System.out.println(PREFIX + "Registered Menubot successfully!");
        return bot;
    }

    public Main(){
        data = Data.read();
        data.save();
        if (data.getRestaurantSubDomain() == null || data.getToken() == null || data.getUsername() == null){
            System.err.println("You need to fill out the bot and restaurant credentials in order to use this bot!");
            System.exit(0);
        }
        this.timer = new Timer();
        restaurant = new Restaurant(data.getRestaurantSubDomain());

        fetchMenu();

        Date now = Calendar.getInstance().getTime();
        scheduleSending(new Date(now.getYear(), now.getMonth(), now.getDate() + 1, 8, 0, 0));
    }

    private void sendMessage(String message, long group){
        try {
            execute(new SendMessage(group, message));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void processCommand(String command, Long group){
        switch (command.toLowerCase()){
            case "start":
                sendMessage("Hey there! Use /subscribe to subscribe to the Menu notifications. For other Stuff use /help!", group);
                break;
            case "subscribe":
                if (data.isSubscribed(group))  sendMessage("You are already subscribed to the Menu notifications. Use  /unsubscribe to unsubscribe from the notifications.", group);
                else {
                    data.addSubscription(group);
                    data.save();
                    sendMessage("You are now successfully subscribed to the Menu notifications.", group);
                }
                break;
            case "unsubscribe":
                if (data.isSubscribed(group)){
                    data.removeSubscription(group);
                    data.save();
                    sendMessage("You are not subscribed to the Menu notifications anymore.", group);
                }else   sendMessage("You are not subscribed to the Menu notifications. Use /subscribe to subscribe to it.", group);
                break;
            case "menu":
                printMenu(group);
                break;
            case "info":
                printInfo(group);
                break;
            case "about":
                printAbout(group);
                break;
            case "help":
                sendMessage("The Following commands are Supported: \n" +
                        "  /help\n" +
                        "    Display this Message.\n" +
                        "  /start\n" +
                        "    Display the start Message.\n" +
                        "  /subscribe\n" +
                        "    Subscribe to the Menu notifications.\n" +
                        "  /unsubscribe\n" +
                        "    Unsubscribe from the Menu notifications.\n" +
                        "  /menu\n" +
                        "    Get the current Menu\n" +
                        "  /info\n" +
                        "    Get info about the Restaurant the bot is linked to\n" +
                        "  /about\n" +
                        "    Get info about this bot and its creators", group);
                break;
        }
    }

    private void fetchMenu(){
        try {
            restaurant.fetchMenues();
            MenuDay day = restaurant.getMenuWeek().getDays()[0];
            StringBuilder sb = new StringBuilder();
            sb.append("Today, the following menues are available:\n");
            for (Menu menue : day.getMenues()) {
                sb.append("\n");
                sb.append(convertMenu(menue));
            }
            this.currentMenuString = sb.toString();
        } catch (IOException e) {
            System.out.println(PREFIX + "Failed to fetch the new Menues!");
            e.printStackTrace();
        }

    }

    private void printInfo(long group){
        try {
            restaurant.fetchData();
            StringBuilder sb = new StringBuilder();
            sb.append("This bot is linked to ");
            sb.append(restaurant.getName());
            sb.append("\n");
            sb.append("You find the real menuplan at:\n");
            sb.append("  ");
            sb.append(restaurant.getSubdomain());
            sb.append(".sv-restaurant.ch");
            sendMessage(sb.toString(), group);
        } catch (IOException e) {
            sendMessage("Sorry but an internal Error occurred!", group);
            e.printStackTrace();
        }
    }

    private void printAbout(long group){
        String s = "This bot and its sv restaurant API was programmed by: \n" +
                "  Virt - https://github.com/VirtCode\n" +
                "and was inspired by: \n" +
                "  _WhySoBad - https://github.com/WhySoBad";
        sendMessage(s, group);
    }

    private void printMenu(long group){
        sendMessage(currentMenuString, group);
    }

    private String convertMenu(Menu menu){
        StringBuilder sb = new StringBuilder();
        sb.append("  " + menu.getTitle() + "\n");
        sb.append("    " + menu.getIngredients() + "\n");
        sb.append("    Additional: " + menu.getAdditionalInfo() + "\n");
        sb.append("    Is Vegetarian: " + menu.isVegetarian() + "\n");
        return sb.toString();
    }

    private void scheduleSending(Date when) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd - hh:mm:ss");
        System.out.println(PREFIX + "Scheduling next Sending at: " + format.format(when));

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(PREFIX + "Running next Sending");
                for (Long subscription : data.getSubscriptions()) {
                    printMenu(subscription);
                }
                Date nextTarget = incrementDay(when);
                scheduleSending(nextTarget);
            }
        }, when);
    }

    private Date incrementDay(Date day) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(day);
        cal.add(Calendar.DATE, 1);
        return cal.getTime();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            if (message.startsWith("/")){
                processCommand(message.substring(1), update.getMessage().getChatId());
            }
        }
    }

    @Override
    public String getBotUsername() {
        return data.getUsername();
    }

    @Override
    public String getBotToken() {
        return data.getToken();
    }
}
