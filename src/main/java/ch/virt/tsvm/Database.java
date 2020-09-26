package ch.virt.tsvm;

import ch.virt.svrestaurant.api.menu.Menu;
import ch.virt.svrestaurant.api.menu.MenuDay;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * @author VirtCode
 * @version 1.0
 */
public class Database {
    private static final String TAG = "[Database] ";

    private static final String menuDataName = "Menus";
    private static final String dayDataName = "Days";

    MongoDatabase database;

    MongoCollection<Document> menuData;
    MongoCollection<Document> dayData;

    private boolean connected = false;

    public void connect(String uris) {
        System.out.println(TAG + "Trying to connect to the database");
        if (uris == null || uris.equals("")) {
            System.out.println(TAG + "Please provide a Database access link!");
            connected = false;
            return;
        }
        MongoClientURI uri = new MongoClientURI(uris);
        MongoClient client = new MongoClient(uri);
        database = client.getDatabase(uri.getDatabase());
        createNecessary();
        connected = true;
        System.out.println(TAG + "Successfully connected to the database");
    }

    public void createNecessary() {
        boolean menusAvailable = false;
        boolean daysAvailable = false;

        for (String s : database.listCollectionNames()) {
            if (s.equals(menuDataName)) menusAvailable = true;
            else if (s.equals(dayDataName)) daysAvailable = true;
        }

        if (!menusAvailable) {
            System.out.println(TAG + "Creating Menu Data Collection");
            database.createCollection(menuDataName);
        }

        if (!daysAvailable) {
            System.out.println(TAG + "Creating Day Data Collection");
            database.createCollection(dayDataName);
        }

        menuData = database.getCollection(menuDataName);
        dayData = database.getCollection(dayDataName);
    }

    public void newMenus(Calendar when, MenuDay menus) {
        if (!connected) return;
        System.out.println(TAG + "Inserting new Menus into the database");
        when.set(Calendar.SECOND, 0);
        when.set(Calendar.MINUTE, 0);
        when.set(Calendar.HOUR, 0);
        when.set(Calendar.MILLISECOND, 0);
        when.set(Calendar.HOUR_OF_DAY, 0);

        Document[] docs = new Document[menus.getMenus().length];
        for (int i = 0; i < menus.getMenus().length; i++) {
            docs[i] = newMenu(when, menus.getMenus(), i);
        }

        for (Document doc : docs) {
            ArrayList<Document> comesWith = new ArrayList<Document>(Arrays.asList(docs));
            comesWith.remove(doc);
            ArrayList<ObjectId> ids = new ArrayList<>();
            for (Document document : comesWith) {
                ids.add(document.getObjectId("_id"));
            }
            doc.append("with", ids);
        }

        menuData.insertMany(Arrays.asList(docs));

        ArrayList<ObjectId> ids = new ArrayList<>();
        for (Document doc : docs) {
            ids.add(doc.getObjectId("_id"));
        }

        Document dayDocument = new Document();
        dayDocument.append("_id", when.getTime().getTime());
        dayDocument.append("date", when.getTime());
        dayDocument.append("menus", ids);

        dayData.insertOne(dayDocument);
    }

    public Document newMenu(Calendar when, Menu[] menus, int index) {
        Menu menu = menus[index];

        Document document = new Document();
        document.append("_id", new ObjectId());

        document.append("date", when.getTime());
        document.append("index", index);
        document.append("title", menu.getTitle());
        document.append("description", menu.getIngredients());
        document.append("extra", menu.getAdditionalInfo());
        document.append("vegetarian", menu.isVegetarian());
        document.append("price", createPrices(menu.getPrices()));

        return document;
    }

    public ArrayList<Document> createPrices(HashMap<String, Float> prices){
        ArrayList<Document> doc = new ArrayList<>();
        for (Map.Entry<String, Float> stringFloatEntry : prices.entrySet()) {
            Document price = new Document();
            price.append("group", stringFloatEntry.getKey());
            price.append("price", stringFloatEntry.getValue());
            doc.add(price);
        }

        return doc;
    }

}
