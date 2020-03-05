# TelegramSVMenu
This is an easy to use Telegram Bot to make you aware of the current Menu
## Disclaimer
This bot can only be used for restaurants of the SV Group!
This bot must also be hosted on a device or server.
## Features
* Easy to setup
* User can subscribe to daily notifications of the current menu.
* User can unsubscribe from the notifications.
* User can get information about the current and following menu on demand.
* User can get information about who hosts the bot, who made the bot, and which restaurant the bot is linked to.
* Highly customizable, allowing the host to configure almost everything.
* All messages of the bot can be configured, allowing to translate it into various languages
## Setup
### Download and Installation
First you need to get your hands on the executable of the Bot.
You can either build one yourself by cloning the repository or download the executable.
To download the executable, you should go into the release section and download the latest jar.

When you have got your runnable jar, you should put it into a directory where the bot is supposed to run in.
After that you can execute it via the command line with
``` java -jar TelegramSVMenu.jar ```

After the first start, it should create a data.json there you have to first enter your credentials of your [bot](https://core.telegram.org/bots) and then enter your subdomain (```https://[subdomain].sv-restaurant.ch/```) of your restaurant of choice (as shown in the configuration section).
You now can use your bot on the default settings.
### Configuration
To configure your bot, you need to edit the already mentioned data.json. In there, it should look like this:
``` js
{
  "botToken": "a1b2c3:abc",           //Authentication token of your bot.
  "botUsername": "@abc",              //Username of your bot.
  "restaurantSubDomain": "abc",       //Subdomain of your desired restaurant.
  "hostString": "",                   //String shown when the user types /host.
  "menuNames": [],                    //Names of the different menues (Default: Menu 1, Menu 2 ...).
  "maxMenues": 10,                    //Maximal amount of menues printed to the user.
  "printAdditional": false,           //Whether the bot should print the additional information about a menu.
  "menuBlacklist": [],                //Blacklisted keywords to prevent a notification to be sent with a certain menu that containes one of them.
  "useCustomStrings": false,          //Whether the bot should use custom by the host provided strings.
  "schedulingHour": 8,                //When the bot should send the notification to the user (Example = 8AM).
  "enableReload": false,              //Whether the /reload command is enabled to read in new config and strings on the go.
  "subscriptions": []                 //Group IDs which are subscribed to the menu notifiactions (Set by the bot)
}
```
#### Custom Strings
To start using custom strings you should enable the useCustomStrings option in the data.json.
You now should also place a file called strings.json next to the data.json (A template with the default strings can be found in the /default folder).
You now can go and customize those strings as you like.
For all strings not mentioned in the strings.json file, the default english string will be used. 
For quick debugging, I suggest enabling the /reload command to reload the strings on runtime.
