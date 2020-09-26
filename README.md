# TelegramSVMenu
This is an easy to use Telegram Bot to make you aware of the current Menu
## Disclaimer
This bot can only be used for restaurants of the SV Group!
This bot must also be hosted on a device or server. Additionally, this bot is a community driven Project and is not associated with the SV-Group itself, nor is it official at any point.
## Features
* Easy to setup
* User can subscribe to daily notifications of the current menu.
* User can unsubscribe from the notifications.
* User can get information about the current and following menu on demand.
* User can get information about who hosts the bot, who made the bot, and which restaurant the bot is linked to.
* Highly customizable, allowing the host to configure almost everything.
* All messages of the bot can be configured, allowing to translate it into various languages
* Allows the host to inject menu data into own database, allowing a recorded history of previous menus
## Version
The most current version is **2.2**
## Setup
### Download and Installation
First you need to get your hands on the executable of the Bot.
You can either build one yourself by cloning the repository or download the executable.
To download the executable, you should go into the release section and download the latest jar.

When you have got your runnable jar, you should put it into a directory where the bot is supposed to run in.
After that you can execute it via the command line with
``` java -jar TelegramSVMenu.jar ```

After the first start, it should create a data.json there you have to first enter your credentials of your [bot](https://core.telegram.org/bots "Official Telegram Docs") and then enter your subdomain and language (```https://[subdomain].sv-restaurant.ch/[language]```) ("en" is ENGLISH, "de" is GERMAN) of your restaurant of choice (as shown in the configuration section).
You now can use your bot on the default settings.
### Configuration
To configure your bot, you need to edit the already mentioned data.json. In there, it should look like this:
``` js
{
  "botToken": "a1b2c3:abc",           //Authentication token of your bot.
  "botUsername": "@abc",              //Username of your bot.
  "restaurantSubDomain": "abc",       //Subdomain of your desired restaurant.
  "restaurantSubMenuplan": "abc",     //Optional: Submenuplan of your desired restaurant. If not provided first Menuplan is used.
  "restaurantLang": "ENGLISH",        //Language of the website the menuplan in on. "ENGLISH" or "GERMAN".
  "hostString": "",                   //String shown when the user types /host.
  "menuNames": [],                    //Names of the different menues (Default: Menu 1, Menu 2 ...).
  "maxMenues": 10,                    //Maximal amount of menues printed to the user.
  "printAdditional": false,           //Whether the bot should print the additional information about a menu.
  "printVegetarian": false,           //Whether the bot should print whether the menu is vegetarian.
  "menuBlacklist": [],                //Blacklisted keywords to prevent a notification to be sent with a certain menu that contains one of them.
  "useCustomStrings": false,          //Whether the bot should use custom by the host provided strings.
  "doWeekends": false,                //Whether the bot should send the Menu Notifications on Weekends
  "schedulingHour": 8,                //When the bot should send the notification to the user (Example = 8AM).
  "enableReload": false,              //Whether the /reload command is enabled to read in new config and strings on the go.
  "enableDatabase": false,            //Whether the bot should inject the data into a database
  "databaseClientURI": "abc",         //The MongoDB Client URI of the database the bot should inject stuff into (optional)
  "subscriptions": []                 //Group IDs which are subscribed to the menu notifications (Set by the bot)
}
```
#### Custom Strings
To start using custom strings you should enable the useCustomStrings option in the data.json.
You now should also place a file called strings.json next to the data.json (A template with the default strings can be found in the /default folder).
You now can go and customize those strings as you like.
For all strings not mentioned in the strings.json file, the default english string will be used. 
For quick debugging, I suggest enabling the /reload command to reload the strings on runtime.
#### Database
Disclaimer: This bot does only support MongoDB databases! <br>
If you want to use a database, you should first set the enableDatabase option to true. Then you should somehow get your MongoClientURI of your cluster and instert it into the databaseClientURI option. The specified user should have write permissions, and the default provided database should be where you want to have your data. The bot will create the requred Collections automatically.<br>
When the bot now sends the daily menu notification, it will insert the data into the database. There will be a "Days" collection, showing all the days recorded with ids pointing to the individual menu in the "Menus" collection. Once you have a few records, it should be pretty self explainatory how to process this data, so I am not providing an example here. If you still struggle, you can of course have a look at the code or write an issue.
## License
This project is licenced under the MIT Licence. Learn more about it in the LICENCE file.
