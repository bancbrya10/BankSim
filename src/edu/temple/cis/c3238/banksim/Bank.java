package edu.temple.cis.c3238.banksim;

/**
 * @author Cay Horstmann
 * @author Modified by Paul Wolfgang
 * @author Modified by Charles Wang
 */

public class Bank {

    public static final int NTEST = 10;
    private final Account[] accounts;
    private long ntransacts = 0;
    private final int initialBalance;
    private final int numAccounts;
    private boolean flag = false;
    private int counter = 0;

    public Bank(int numAccounts, int initialBalance) {
        this.initialBalance = initialBalance;
        this.numAccounts = numAccounts;
        accounts = new Account[numAccounts];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account(this, i, initialBalance);
        }
        ntransacts = 0;
    }

    public void transfer(int from, int to, int amount) {
//        accounts[from].waitForAvailableFunds(amount);
        if (accounts[from].withdraw(amount)) {
            accounts[to].deposit(amount);
        }
        // if flag is already set, then we have to test
        if(flag) {
            counter++; // increment num of threads that we have waiting
            waitForFlagFalse();
        } else {
            // Otherwise, check if it is time to test
            // if it is, then wait this thread until counter == numthreads
            if (shouldTest()) {
                flag = true;
                counter++;
                waitForAllAccounts();
                Sum sum = new Sum(this);
                sum.start();
                counter = 0;
                flag = false;
                notifyAll();
            }
        }
    }

    public void test() {
        int sum = 0;
        for (Account account : accounts) {
            System.out.printf("%s %s%n", 
                    Thread.currentThread().toString(), account.toString());
            sum += account.getBalance();
        }
        System.out.println(Thread.currentThread().toString() + 
                " Sum: " + sum);
        if (sum != numAccounts * initialBalance) {
            System.out.println(Thread.currentThread().toString() + 
                    " Money was gained or lost");
            System.exit(1);
        } else {
            System.out.println(Thread.currentThread().toString() + 
                    " The bank is in balance");
        }
    }

    public int size() {
        return accounts.length;
    }
    
    // so that only one account can set flag at any given time
    synchronized public boolean shouldTest() {
        return ++ntransacts % NTEST == 0 && !flag;
    }

    public synchronized void waitForAllAccounts() {
        while(counter < accounts.length) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
    }

    public synchronized void waitForFlagFalse() {
        while(flag) {
            try {
                wait();
            } catch (InterruptedException e) {

            }
        }
    }

}
