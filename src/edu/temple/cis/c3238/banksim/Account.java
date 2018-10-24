package edu.temple.cis.c3238.banksim;

/**
 * @author Cay Horstmann
 * @author Modified by Paul Wolfgang
 * @author Modified by Charles Wang
 */
public class Account {

    private volatile int balance;
    private final int id;
    private final Bank myBank;

    public Account(Bank myBank, int id, int initialBalance) {
        this.myBank = myBank;
        this.id = id;
        balance = initialBalance;
    }

    public synchronized int getBalance() {
        return balance;
    }

    public synchronized boolean withdraw(int amount) {
        int currentBalance = balance;
        int newBalance = currentBalance - amount;
        balance = newBalance;
        return true;
    }

    public synchronized void deposit(int amount) {
        int currentBalance = balance;
        int newBalance = currentBalance + amount;
        balance = newBalance;
        notifyAll();
    }

    public synchronized void waitForBalanceAvailable(int amount) {
        while(amount > balance && myBank.isOpen()) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
    }
    
    @Override
    public synchronized String toString() {
        return String.format("Account[%d] balance %d", id, balance);
    }
}
