package com.computacion1.Call;

import java.net.SocketException;
import java.util.HashMap;

import com.computacion1.ClientHandler;

public class CallController {
    private HashMap<String, Call> currentActiveCalls;

    public CallController(){
        currentActiveCalls = new HashMap<>();
    }

    public int startCall(ClientHandler callStarter) throws IllegalArgumentException {
        String name = callStarter.getClient().getUsername();
        try {
            Call newCall = new Call();
            newCall.run();
            newCall.addMember(callStarter);
            currentActiveCalls.put(name, newCall);
            return newCall.getCallPort();
        } catch (SocketException e) {
            e.printStackTrace();
            throw new IllegalArgumentException();
        }

    }

    public int joinCall(ClientHandler userToJoin, String callStarter) {
        Call call = currentActiveCalls.get(callStarter);
        call.addMember(userToJoin);
        return call.getCallPort();
    }

    public void leaveCall(ClientHandler clientToLeave, String callStarter) {
        Call call = currentActiveCalls.get(callStarter);
        call.removeMember(clientToLeave);
        if (call.isEmpty()) {
            currentActiveCalls.remove(callStarter);
        }
    }

    public void shutDownCall(String callStarter) {
        Call call = currentActiveCalls.get(callStarter);
        call.shutDown();
        currentActiveCalls.remove(callStarter);
    }

}
