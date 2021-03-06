package com.daratus.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.daratus.node.console.ConsoleMenssenger;
import com.daratus.node.domain.Node;
import com.daratus.node.domain.NullTask;

/**
 * 
 * @author Zilvinas Vaira
 *
 */
public class BlockedStateTest {

    private final String originalName = "TestName";
    
    private Node original;
    
    private final String newName = "TestNewName";
    
    private Node newNode;
    
    private AuthenticationState initialState;

    private NodeContextMockup context;

    private OperationalState operationalState;

    private BlockedState blockedState;
    
    @Before
    public void setUp() throws Exception {
        original = new Node();
        original.setShortCode(originalName);
        
        newNode = new Node();
        newNode.setShortCode(newName);
        
        initialState = new AuthenticationState();
        context = new NodeContextMockup();
        context.setMessenger(new ConsoleMenssenger(System.out, System.out));
        operationalState = new OperationalState(initialState);
        blockedState = new BlockedState(initialState, operationalState);
    }

    /**
     * 
     */
    @Test
    public void testHandleMissingConstraints() {
        blockedState.handle(context);
        assertNotNull(context.getCurrentState());
        assertEquals(initialState, context.getCurrentState());

        context.setCurrentState(blockedState);
        blockedState.handle(context);
        assertNotNull(context.getCurrentState());
        assertEquals(initialState, context.getCurrentState());
        
        context.setCurrentState(blockedState);
        context.setNode(original);
        blockedState.handle(context);
        assertNotNull(context.getCurrentState());
        assertEquals(operationalState, context.getCurrentState());
    }
    
    /**
     * 
     */
    @Test
    public void testHandleBlockedState() {
        context.setCurrentState(blockedState);
        context.setNode(original);
        context.setBlocked(true);
        assertNotNull(context.getCurrentState());
        assertEquals(blockedState, context.getCurrentState());
        
        context.setNode(newNode);
        blockedState.handle(context);
        assertNotNull(context.getCurrentState());
        assertEquals(blockedState, context.getCurrentState());
        assertEquals(originalName, context.getNode().getShortCode());
    }
    
    /**
     * 
     */
    @Test
    public void testHandleLogout() {
        context.setCurrentState(blockedState);
        context.setNode(original);
        context.setBlocked(true);
        assertNotNull(context.getCurrentState());
        assertEquals(blockedState, context.getCurrentState());
        
        context.logout();
        blockedState.handle(context);
        assertNotNull(context.getCurrentState());
        assertEquals(initialState, context.getCurrentState());
    }

    /**
     * 
     */
    @Test
    public void testHandleStop() {
        context.setCurrentState(operationalState);
        operationalState.setNextState(blockedState);
        context.setNode(original);
        context.setBlocked(true);
        assertNotNull(context.getCurrentState());
        assertEquals(blockedState, context.getCurrentState());
        
        try {
            Thread.sleep(3 * NullTask.SECONDS_CONVERSION_RATE / 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        context.setBlocked(false);
        assertNotNull(context.getCurrentState());
        assertEquals(operationalState, context.getCurrentState());
        
    }
    
}
