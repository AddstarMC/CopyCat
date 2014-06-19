package au.com.addstar.copycat.commands;

import au.com.addstar.monolith.command.RootCommandDispatcher;

public class CopyCatCommand extends RootCommandDispatcher
{
	public CopyCatCommand()
	{
		super("");
		
		registerCommand(new CreateCommand());
		registerCommand(new SetStationCommand());
		registerCommand(new SetupCommand());
		registerCommand(new SetCommand());
		registerCommand(new SetPatternStationCommand());
		registerCommand(new DeleteCommand());
		registerCommand(new EditorCommand());
	}
}
