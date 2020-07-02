package ch.virt.tsvm;

import ch.virt.svrestaurant.api.Restaurant;
import ch.virt.svrestaurant.api.menu.Menu;
import ch.virt.svrestaurant.api.menu.MenuDay;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This is the main class of this project.
 * Here is where the magic happens.
 * @author VirtCode
 * @version 1.0
 */
public class Main extends TelegramLongPollingBot {
    public static final String VERSION_TAG = "Release 2.0";

    public static final String TAG = "[MenuBot] ";

    private Restaurant restaurant;
    private Timer timer;
    private Data data;
    private Translation translation;
    private Database database;

    public static void main(String[] args) {
        create();
    }
    /**
     * Creates and registers the bot
     * Then starts it
     */
    public static void create(){
        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();
        Main bot = new Main();

        try {
            botsApi.registerBot(bot);
        } catch (TelegramApiRequestException e) {
            System.err.println(TAG + "Failed to register Telegram Bot");
        }

        System.out.println(TAG + "Registered Telegram Bot successfully!");
    }
    /**
     * Starts the bot
     */
    public Main(){
        data = Data.read();
        data.save();

        database = new Database();
        if(data.isEnableDatabase()) database.connect(data.getDatabaseClientURI());

        if (data.getRestaurantSubDomain() == null || data.getBotToken() == null || data.getBotUsername() == null){
            System.err.println(TAG + "You need to fill out the bot and restaurant credentials in order to use this bot!");
            System.exit(0);
        }

        if (data.isUseCustomStrings()) translation = Translation.read();
        else translation = new Translation();

        this.timer = new Timer();

        restaurant = new Restaurant(data.getRestaurantSubDomain());

        Calendar now = Calendar.getInstance();
        now.set(Calendar.HOUR, data.getSchedulingHour());
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        scheduleSending(incrementDay(now));
    }

    /**
     * Schedules the next Menu notification
     * @param when when to send it
     */
    private void scheduleSending(Calendar when) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd - hh:mm:ss");
        System.out.println(TAG + "Scheduling next Sending at: " + format.format(when.getTime()));

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(TAG + "Running next Sending");
                String menues = fetchMenu(Calendar.getInstance(), true);
                for (String s : data.getMenuBlacklist()) {
                    if (menues.toLowerCase().contains(s.toLowerCase())) {
                        scheduleSending(incrementDay(when));
                        return;
                    }
                }
                for (Long subscription : data.getSubscriptions()) {
                    sendMessage(menues, subscription);
                }
                scheduleSending(incrementDay(when));
            }
        }, when.getTime());
    }
    /**
     * Increments the given date
     * @param day date to increment
     * @return incremented date
     */
    private Calendar incrementDay(Calendar day) {
        day.add(Calendar.DATE, 1);
        return day;
    }

    @Override
    public String getBotUsername() {
        return data.getBotUsername();
    }
    @Override
    public String getBotToken() {
        return data.getBotToken();
    }

    /**
     * Cleans and sends a message into a group
     * @param message message to send
     * @param group group to send in
     */
    private void sendMessage(String message, long group){
        try {
            execute(new SendMessage(group, cleanString(message)).setParseMode("MarkdownV2"));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    /**
     * Prepares a String for botusage
     * @param s String to clean
     * @return cleaned String
     */
    private String cleanString(String s){
        return s.replace(".", "\\.").replace("!", "\\!").replace("-", "\\-");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            if (message.startsWith(translation.getCommandIndicator())){
                System.out.println(TAG + "Command \"" + message.substring(1) + "\" issued from " + update.getMessage().getChat().getFirstName() + "[" + update.getMessage().getChatId() + "]");
                processCommand(message.substring(1), update.getMessage().getChatId());
            }
        }
    }

    /**
     * Processes a command sent by the user
     * @param command command to process
     * @param group group sent in
     */
    public void processCommand(String command, Long group){
        switch (command.toLowerCase()){
            case "start":
                sendMessage(translation.getStartMessage(), group);
                break;
            case "subscribe":
                if (data.isSubscribed(group))  sendMessage(translation.getSubscribeAlready(), group);
                else {
                    data.addSubscription(group);
                    data.save();
                    sendMessage(translation.getSubscribeSuccess(), group);
                }
                break;
            case "unsubscribe":
                if (data.isSubscribed(group)){
                    data.removeSubscription(group);
                    data.save();
                    sendMessage(translation.getUnsubscribeSuccess(), group);
                }else   sendMessage(translation.getUnsubscribeAlready(), group);
                break;
            case "menu":
                sendMessage(fetchMenu(Calendar.getInstance(), false), group);
                break;
            case "tomorrow":
                sendMessage(fetchMenu(incrementDay(Calendar.getInstance()), false), group);
                break;
            case "info":
                printInfo(group);
                break;
            case "about":
                printAbout(group);
                break;
            case "host":
                printHost(group);
                break;
            case "help":
                sendMessage(translation.getHelp(), group);
                break;
            case "version":
                sendMessage(translation.getVersionPrefix() + VERSION_TAG, group);
                break;
            case "reload":
                if (data.isEnableReload()){
                    data = Data.read();
                    translation = Translation.read();
                }
                break;
            default: sendMessage(translation.getHelpInstructor(), group);
        }
    }

    /**
     * Fetches a Menu from the site and turns it into a String
     * @param date date to print
     * @return converted string
     */
    private String fetchMenu(Calendar date, boolean daily){
        try {
            restaurant.fetchMenues();
            MenuDay day = restaurant.getMenuWeek().getDay(date.get(Calendar.DATE), date.get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR)); // January = 0
            if (day == null) return translation.getMenuOffline();
            if(daily) database.newMenus(date, day);
            StringBuilder sb = new StringBuilder();
            sb.append("__");
            sb.append(new SimpleDateFormat(translation.getMenuDateFormat()).format(date.getTime()));
            sb.append("__");
            int i = 0;
            for (Menu menue : day.getMenues()) {
                sb.append(Translation.newLine);
                sb.append("*");
                sb.append(data.getMenuName(i));
                sb.append("*");
                sb.append(Translation.newLine);
                sb.append(convertMenu(menue));
                i++;
                if (i == data.getMaxMenues()) break;
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return translation.getMenuOffline();
        }
    }
    /**
     * Converts a Menu to a String
     * @param menu Menu to convert
     * @return convertedString
     */
    private String convertMenu(Menu menu){
        StringBuilder sb = new StringBuilder();
        sb.append(translation.getTabSpaces());
        sb.append(menu.getTitle());
        sb.append(Translation.newLine);
        sb.append(translation.getTabSpaces());
        sb.append(menu.getIngredients());
        sb.append(Translation.newLine);
        if(data.isPrintAdditional()){
            sb.append(translation.getTabSpaces());
            sb.append(translation.getMenuAdditional());
            sb.append(menu.getAdditionalInfo());
            sb.append(Translation.newLine);
        }
        sb.append(translation.getTabSpaces());
        sb.append(translation.getMenuVegetarian());
        sb.append(menu.isVegetarian());
        sb.append(Translation.newLine);
        return sb.toString();
    }

    /**
     * Prints the info message
     * @param group group to print in
     */
    private void printInfo(long group){
        try {
            restaurant.fetchData();
            String sb = translation.getInfoRestaurant() +
                    restaurant.getName() +
                    Translation.newLine +
                    translation.getInfoDomain() +
                    Translation.newLine +
                    translation.getTabSpaces() +
                    restaurant.getSubdomain() +
                    Restaurant.URL_SUFFIX;
            sendMessage(sb, group);
        } catch (IOException e) {
            sendMessage(translation.getInfoOffline(), group);
            e.printStackTrace();
        }
    }

    /**
     * Prints the host message
     * @param group group to print in
     */
    private void printHost(long group){
        String s = translation.getHostString() + data.getHostString();
        sendMessage(s, group);
    }

    /**
     * Prints the about message
     * @param group group to print in
     */
    private void printAbout(long group){
        String s = "This bot and its sv restaurant API were programmed by:" + Translation.newLine +
                "  Virt - https://github.com/VirtCode" + Translation.newLine +
                "who was inspired by:" + Translation.newLine +
                "  WhySoBad - https://github.com/WhySoBad";
        sendMessage(s, group);
    }

}
