package com.daratus.node.console;

import java.util.HashMap;
import java.util.Map;

import com.daratus.node.NodeContext;

public class CommandFactory {

    private Map<String, AbstractCommand> commandPool = new HashMap<String, AbstractCommand>();
    
    private DefaultCommand unrecognizedCommand = new DefaultCommand();
    
    private NodeContext context;
    
    public CommandFactory(NodeContext context) {
        this.context = context;
    }
    
    public AbstractCommand createCommand(String commandLine){
        String [] commandParameters = commandLine.split(" ");
        String commandToken = commandParameters[0];
        AbstractCommand command = unrecognizedCommand;
        if( commandPool.containsKey(commandToken)){
            command = commandPool.get(commandToken);
        }else{
            command = createCommand(commandParameters);
            commandPool.put(commandToken, command);
        }
        return command;
    }
    
    private AbstractCommand createCommand(String [] commandParameters){
        String commandToken = commandParameters[0];
        if(commandToken.equals(AbstractCommand.HELP)){
            return new DefaultCommand(commandParameters);
        }else if(commandToken.equals(AbstractCommand.LOGIN)){
            return new NodeAPICommand(commandParameters, APICommand.NODE_PATH, context);
        }else if(commandToken.equals(AbstractCommand.REGISTER)){
            return new NodeAPICommand(commandParameters, APICommand.NODE_PATH, context);
        }else if(commandToken.equals(AbstractCommand.NEXT)){
            return new NextTaskAPICommand(commandParameters, APICommand.NEXT_TASK_PATH, context);
        }else if(commandToken.equals(AbstractCommand.EXECUTE)){
            return new TaskCommand(commandParameters, context);
        }else if(commandToken.equals(AbstractCommand.EXIT)){
            return new DefaultCommand(commandParameters);
        }else{
            return unrecognizedCommand;
        }
    }
    
}
