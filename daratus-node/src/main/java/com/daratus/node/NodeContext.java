package com.daratus.node;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;

import org.jsoup.helper.W3CDom;

import com.daratus.node.console.APICommand;
import com.daratus.node.console.AbstractCommand;
import com.daratus.node.domain.NullTask;
import com.daratus.node.domain.Task;
import com.daratus.node.domain.TaskObserver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NodeContext implements TaskObserver, Runnable{
    
    private APIConnector apiConnector;
    
    private ScrapingConnector scrapingConnector;

    private ObjectMapper mapper;
    
    private W3CDom w3cDom;
    
    private XPath xPath;
    
    protected NodeState currentState = null;
    
    private boolean isBlocked = false;
    
    private String name = null;
    
    private Task currentTask;

    private final Task nullTask = new NullTask();
    
    private Logger logger;
    
    public NodeContext(APIConnector apiConnector, ScrapingConnector scrapingConnector, ObjectMapper mapper, W3CDom w3cDom, XPath xPath) {
        this.apiConnector = apiConnector;
        this.scrapingConnector = scrapingConnector;
        this.mapper = mapper;
        this.w3cDom = w3cDom;
        this.xPath = xPath;
        nullTask.addTaskObserver(this);
        setCurrentTask(nullTask);
        logger = getLogger(this.getClass().getSimpleName());
    }
    
    public Logger getLogger(String className){
        return Logger.getLogger(className);
    }

    public APIConnector getAPIConnector() {
        return apiConnector;
    }
    
    public ScrapingConnector getScrapingConnector() {
        return scrapingConnector;
    }
    
    public ObjectMapper getMapper() {
        return mapper;
    }
    
    public W3CDom getW3cDom() {
        return w3cDom;
    }
    
    public XPath getxPath() {
        return xPath;
    }
    
    public void handleCurrentState(){
        currentState.handle(this);
    }

    public void setCurrentState(NodeState state){
        this.currentState = state;
    }
    
    public NodeState getCurrentState() {
        return currentState;
    }
    
    public void setBlocked(boolean isBlocked){
        if(!this.isBlocked && !isBlocked){
            logger.warning("Can not execute stop command. It is already stoped!");
        }else if(!isBlocked){
            System.out.println("Stop request queued... please wait!");
        }
        this.isBlocked = isBlocked;
    }
    
    public boolean isBlocked(){
        return isBlocked;
    }
    
    public void authenticate(String apiPath, String name){
        if(!isAuthenticated()){
            System.out.println("Sending authetication request to Daratus API for node ID '" + name + "'!");
            String jsonResponse = apiConnector.sendRequest(apiPath + name, RequestMethod.GET);
            if(jsonResponse != null){
                setName(name);
                System.out.println("Found node ID '" + name + "' on server! Succesfuly authenticated!");
            }
        }else{
            logger.warning("User is already authenticated! Please use '" + AbstractCommand.LOGOUT + "' first!");
        }
    }
    
    public void setName(String name) {
        if(this.name == null){
            this.name = name;
        }
    }
    
    public String getName() {
        return name;
    }

    public boolean isAuthenticated(){
        return name != null;
    }
    
    public void logout(){
        if(isAuthenticated()){
            System.out.println("Succesfully loged out node ID '" + name + "'!");
            this.name = null;
        }else{
            logger.warning("Could not logout! There is no node authenticated!");
        }
    }
    
    protected void getNextTask(String apiPath){
        System.out.println("Sending next taks request to Daratus API for node ID '" + name + "'!");
        String jsonResponse = apiConnector.sendRequest(apiPath + getName(), RequestMethod.GET);
        if(jsonResponse != null){
            try {
                Task task = mapper.readValue(jsonResponse, Task.class);
                task.addTaskObserver(this);
                System.out.println("Got a task '" + task.getClass().getSimpleName() + "' with target URL '" + task + "' from server!");
                setCurrentTask(task);
            } catch (IOException e) {
                logger.warning("Could not read task from server!");
            }
        }else{
            logger.warning("No response from server!");
        }
    }
    
    protected void executeCurrentTask(){
        currentTask.execute(this);
    }
    
    protected void executeTaskLoop(){
        while(isBlocked()){
            System.out.println();
            System.out.println("...looping...");
            executeCurrentTask();
        }
        System.out.println("Task loop has been stopped succesfully!");
    }
    
    public void setCurrentTask(Task currentTask) {
        if(currentTask != null){
            this.currentTask = currentTask;
        }else{
            this.currentTask = nullTask;
        }
    }
    
    private void sendResponse(Task task){
        try {
            apiConnector.setJsonEntity(mapper.writeValueAsString(task));
            System.out.println("Sending task result response to Daratus API to path '" + APICommand.NEXT_TASK_PATH + getName() + "'!");
            apiConnector.sendRequest(APICommand.NEXT_TASK_PATH + getName(), RequestMethod.POST);
            setCurrentTask(nullTask);
            System.out.println("Result has been sent!");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    
    public void notify(Task task) {
        if(task.isCompleted()){
            sendResponse(task);
        }else if(isBlocked()){
            getNextTask(APICommand.NEXT_TASK_PATH);
        }
    }

    public void run() {
        executeTaskLoop();
    }
    
}
