package com.computacion1.UserInput;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserInputHandler {
    private final BlockingQueue<String> generalInputQueue;
    private final ConcurrentHashMap<Thread, BlockingQueue<String>> threadSpecificQueues;
    private final Thread inputThread;
    private volatile boolean running;
    private volatile Thread priorityThread;
    private final AtomicBoolean isPriorityInputExpected;

    public UserInputHandler() {
        this.generalInputQueue = new LinkedBlockingQueue<>();
        this.threadSpecificQueues = new ConcurrentHashMap<>();
        this.running = true;
        this.isPriorityInputExpected = new AtomicBoolean(false);
        this.inputThread = new Thread(this::readInput);
        this.inputThread.setDaemon(true);
        this.inputThread.start();
    }

    private void readInput() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (running) {
                String input = reader.readLine();
                if (input != null) {
                    if (isPriorityInputExpected.get() && priorityThread != null) {
                        BlockingQueue<String> queue = threadSpecificQueues.get(priorityThread);
                        if (queue != null) {
                            queue.put(input);
                            isPriorityInputExpected.set(false);
                            priorityThread = null;
                            continue;
                        }
                    }
                    generalInputQueue.put(input);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getUserInput(String prompt) throws InterruptedException {
        System.out.print(prompt);
        return generalInputQueue.take();
    }

    public String getPriorityThreadSpecificInput(String prompt) throws InterruptedException {
        Thread currentThread = Thread.currentThread();
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        threadSpecificQueues.put(currentThread, queue);
        priorityThread = currentThread;
        isPriorityInputExpected.set(true);
        System.out.print(prompt);
        String input = queue.take();
        threadSpecificQueues.remove(currentThread);
        return input;
    }

    public void shutdown() {
        running = false;
        inputThread.interrupt();
    }
}
