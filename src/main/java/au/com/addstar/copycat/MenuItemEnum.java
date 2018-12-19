package au.com.addstar.copycat;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.Material;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemList;

public class MenuItemEnum<T extends Enum<T>> extends MenuItemList
{
	public MenuItemEnum( Class<T> enumClass, String name, List<String> description, Callback<T> callback, Material displayItem )
	{
		super(name, description, displayItem, getCallback(callback, enumClass), makeOptions(enumClass));
	}
	
	public MenuItemEnum( Class<T> enumClass, String name, Callback<T> callback, Material displayItem)
	{
		super(name, displayItem, getCallback(callback, enumClass), makeOptions(enumClass));
	}
	
	private static <T extends Enum<T>> List<String> makeOptions(Class<T> enumClass)
	{
		EnumSet<T> set = EnumSet.allOf(enumClass);
		ArrayList<String> list = new ArrayList<>(set.size());
		for(T e : set)
			list.add(e.name());
		
		return list;
	}
	
	private static <T extends Enum<T>> Callback<String> getCallback(final Callback<T> wrapped, final Class<T> enumClass)
	{
		Callback<String> callback = new Callback<String>()
		{
			@Override
			public void setValue( String value )
			{
				T e = Enum.valueOf(enumClass, value);
				wrapped.setValue(e);
			}
			
			@Override
			public String getValue()
			{
				T value = wrapped.getValue();
				return value.name();
			}
		};
		
		return callback;
	}
}
