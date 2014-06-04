package au.com.addstar.copycat.flags;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import au.com.addstar.copycat.commands.BadArgumentException;

public abstract class Flag<T>
{
	protected T value;
	
	public T getValue()
	{
		return value;
	}
	
	public void setValue(T value)
	{
		this.value = value;
	}
	
	public abstract T parse(Player sender, String[] args) throws IllegalArgumentException, BadArgumentException;
	public abstract List<String> tabComplete(Player sender, String[] args);
	
	public abstract void save(ConfigurationSection section);
	public abstract void read(ConfigurationSection section) throws InvalidConfigurationException;
	
	public abstract String getValueString();
}
