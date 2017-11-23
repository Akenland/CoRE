package com.KyleNecrowolf.RealmsCore.Extras;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.KyleNecrowolf.RealmsCore.Common.Error;
import com.KyleNecrowolf.RealmsCore.Common.Utils;
import com.KyleNecrowolf.RealmsCore.Prompts.Prompt;

public final class TimeWeatherCommands implements TabExecutor {

    //// Commands
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Target world is the world player is in, or default world if not a player
        World world = (sender instanceof Player) ? ((Player)sender).getWorld() : Bukkit.getWorlds().get(0);


        // If label is "sun", set time to day and clear weather
        if(label.equalsIgnoreCase("sun")){
            if(!sender.hasPermission("realms.time") || !sender.hasPermission("realms.weather")) return Error.NO_PERMISSION.displayChat(sender);

            world.setStorm(false);
            world.setTime(1000);

            Utils.sendActionBar(sender, "Set to daytime, clear weather");
            return true;
        }


        //// Time
        if(command.getName().equalsIgnoreCase("time")){
            // Check permission
            if(!sender.hasPermission("realms.time")) return Error.NO_PERMISSION.displayChat(sender);

            int newTime = 0;

            // If label day, noon, or night, set time immediately
            if(label.equalsIgnoreCase("day")){
                newTime = 1000;
            } else if(label.equalsIgnoreCase("noon")){
                newTime = 6000;
            } else if(label.equalsIgnoreCase("night")){
                newTime = 13000;
            }
            if(newTime>0){
                world.setTime(newTime);
                Utils.sendActionBar(sender, "Time set to "+getTimeString(newTime));
                return true;
            }

            // If no args, prompt
            if(args.length==0){
                Prompt prompt = new Prompt();
                prompt.addQuestion(Utils.infoText+"Current time: "+Utils.messageText+getTimeString(world.getTime()));
                prompt.addAnswer(" 4:30am - Sunrise", "command_time sunrise");
                prompt.addAnswer(" 7:00am - Day", "command_time day");
                prompt.addAnswer("12:00pm - Noon", "command_time noon");
                prompt.addAnswer(" 5:30pm - Sunset", "command_time sunset");
                prompt.addAnswer(" 7:00pm - Night", "command_time night");
                prompt.addAnswer("12:00am - Midnight", "command_time midnight");
                prompt.display(sender);
                return true;
            }

            // If one arg, change time
            if(args.length>=1){
                world.setTime(parseTime(args[0]));
                Utils.sendActionBar(sender, "Time set to "+getTimeString(newTime));
                return true;
            }

            return Error.INVALID_ARGS.displayActionBar(sender);
        }


        //// Weather
        if(command.getName().equalsIgnoreCase("weather")){
            // Check permission
            if(!sender.hasPermission("realms.weather")) return Error.NO_PERMISSION.displayChat(sender);

            // If label storm, set time immediately
            if(label.equalsIgnoreCase("rain")){
                // Toggle current conditions
                if(world.hasStorm()){
                    // If storm, set sun
                    Utils.sendActionBar(sender, "Weather cleared until "+getTimeString(world.getTime()+setWeatherSun(world)));
                    return true;
                } else {
                    Utils.sendActionBar(sender, "Raining until "+getTimeString(world.getTime()+setWeatherRain(world)));
                    return true;
                }
            } else if(label.equalsIgnoreCase("storm")){
                Utils.sendActionBar(sender, "Thundering until "+getTimeString(world.getTime()+setWeatherStorm(world)));
                return true;
            }

            // If no args, show prompt
            if(args.length==0){
                Prompt prompt = new Prompt();
                String conditions = world.hasStorm() ? "Raining" : "Clear";
                prompt.addQuestion(conditions+Utils.infoText+" until "+getTimeString(world.getTime()+world.getWeatherDuration())+", about "+world.getWeatherDuration()/20+" seconds");
                prompt.addAnswer("Clear", "command_weather clear");
                prompt.addAnswer("Rain", "command_weather rain");
                prompt.addAnswer("Thunderstorm", "command_weather storm");
                prompt.display(sender);
                return true;
            }

            // If one arg, set weather
            if(args.length>=1){
                // Set duration, if supplied
                int duration = 0;
                try{duration = args.length==2 ? Integer.parseInt(args[1]) : 0;} catch(NumberFormatException e){}

                switch(args[0]){
                    case "sun": case "clear":
                    Utils.sendActionBar(sender, "Weather cleared until "+getTimeString(world.getTime()+setWeatherSun(world, duration)));
                    return true;

                    case "rain": case "snow":
                    Utils.sendActionBar(sender, "Raining until "+getTimeString(world.getTime()+setWeatherRain(world, duration)));
                    return true;

                    case "storm": case "thunder": case "thunderstorm":
                    Utils.sendActionBar(sender, "Thundering until "+getTimeString(world.getTime()+setWeatherStorm(world, duration)));
                    return true;

                    default:
                    return Error.INVALID_ARGS.displayActionBar(sender);
                }
            }

            return Error.INVALID_ARGS.displayActionBar(sender);
        }

        return false;
    }


    //// Tab completions
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(command.getName().equalsIgnoreCase("time") && args.length==1){
            return Arrays.asList("sunrise", "day", "noon", "sunset", "night", "midnight");
        }

        if(command.getName().equalsIgnoreCase("weather") && args.length==1){
            return Arrays.asList("clear", "rain", "storm");
        }
        
        return null;
    }


    //// Extra methods
    // Get the game time in ticks, from a time string
    private long parseTime(String timeString){
        timeString = timeString.toLowerCase();

        // If the string is in ticks, just return that
        try{
            return Long.parseLong(timeString) % 24000;
        } catch(NumberFormatException e){}

        // Try to parse a hh:mm style time
        try{
            // Parse the time
            LocalTime time = LocalTime.parse(timeString, DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));
            // Convert to seconds, and multiply by 0.0138 (=20/1440) to get Minecraft seconds, 
            // then multiply by 20 to get ticks, then subtract 6000 (minecraft day starts at 6am) to finally get time in ticks
            return (time.toSecondOfDay() * (20/1440) * 20) - 6000;
        } catch(DateTimeParseException e){}

        // See if it's a word
        switch(timeString){
            case "sunrise":
                return 22550;
            case "day":
                return 1000;
            case "noon":
                return 6000;
            case "sunset":
                return 11616;
            case "night":
                return 13000;
            case "midnight":
                return 18000;
            default:
                return 0;
        }
    }

    // Get the time from ticks
    private LocalTime getTime(long ticks){
        // Convert ticks to real seconds
        // Add 6000 (6am offset), divide by 20, divide by 0.0138
        long seconds = (18/5) * ticks + 21600;//((ticks + 6000L) / 20L) / (20L/1440L);

        return LocalTime.ofSecondOfDay(seconds);
    }

    // Get a string with time in both hh:mm and ticks
    private String getTimeString(long ticks){
        /*LocalTime time = getTime(ticks);
        int hour = time.getHour();
        int min = time.getMinute();
        
        // 12-hour time
        String pm = (hour==12) ? "pm" : "am";
        if(hour>12){
            hour -= 12;
            pm = (hour==12) ? "am" : "pm";
        }*/

        return /*hour+":"+min+pm*/getTime(ticks).format(DateTimeFormatter.ofPattern("h:mm a"))+" ("+ticks+" ticks)";
    }


    //// Weather methods
    // Set weather to clear
    private long setWeatherSun(World world, int duration){
        world.setStorm(false);
        // Set duration, if supplied
        if(duration!=0) world.setWeatherDuration(duration);
        // Return ticks until next storm
        return world.getWeatherDuration();
    }
    private long setWeatherSun(World world){
        return setWeatherSun(world, 0);
    }
    // Set weather to rain
    private long setWeatherRain(World world, int duration){
        world.setStorm(true);
        // Set duration, if supplied
        if(duration!=0) world.setWeatherDuration(duration);
        // Return ticks until storm ends
        return world.getWeatherDuration();
    }
    private long setWeatherRain(World world){
        return setWeatherRain(world, 0);
    }
    // Set weather to thunderstorm
    private long setWeatherStorm(World world, int duration){
        world.setStorm(true);
        world.setThundering(true);
        // Set duration, if supplied
        if(duration!=0){
            world.setWeatherDuration(duration);
            world.setThunderDuration(duration);
        }
        // Return ticks until storm ends
        return world.getWeatherDuration();
    }
    private long setWeatherStorm(World world){
        return setWeatherStorm(world, 0);
    }
}