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

    private boolean waitForTest = false;
    //private int threadsPutToSleep = 0;
    private int threadsSleeping = 0;
    private int threadsAwake = 10;


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
            System.out.println("trasnfer has occured");
        }

        if (shouldTest()) {
            waitForTest = true;
        }

        if (waitForTest) {
            // If test() needs to be ran (waitForTest set to true) this method will
            // put each thread to sleep and track threadsPutToSleep/Awake
            sleepTransferThread();
        }

    }

    public synchronized void test() {
        int sum = 0;
        System.out.println("Test Start");
        for (Account account : accounts) {
            System.out.printf("%s %s%n",
                    Thread.currentThread().toString(), account.toString());
            sum += account.getBalance();
        }
        System.out.println(Thread.currentThread().toString()
                + " Sum: " + sum);
        if (sum != numAccounts * initialBalance) {
            System.out.println(Thread.currentThread().toString()
                    + " Money was gained or lost");
            System.exit(1);
        } else {
            System.out.println(Thread.currentThread().toString()
                    + " The bank is in balance");
        }

        //Reset the signal for threads to sleep back to false
        waitForTest = false;
        this.setThreadsSleeping(0);
        //Test is complete, wake up all threads
        notifyAll();
    }

    public int size() {
        return accounts.length;
    }

    public boolean shouldTest() {
        return ++ntransacts % NTEST == 0;
    }

    public synchronized void sleepTransferThread() {

        //Threads are put to sleep upon completion of last transfer
        this.incrThreadsSleeping();
        
        //long threadId = Thread.currentThread().getId();
        //System.out.println("thread " + threadId/*this.getThreadsSleeping()*/ + " put to sleep");
        System.out.println("thread " + this.getThreadsSleeping() + " put to sleep");
        System.out.println("thread " + this.getThreadsAwake() + " still awake");
        System.out.println("ntransactions = " + this.ntransacts);

        //Last thread to sleep creates new thread to run test
        if (this.getThreadsAwake() == 1 /* this.threadsSleeping == NTEST*/) {
            System.out.println("Inside Test Condition");
            Thread t = new Thread(new Sum(this));
            t.start();
        }

        while (waitForTest) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }

    }
    
    public synchronized int getThreadsSleeping() {
        return threadsSleeping;
    }

    public synchronized int getThreadsAwake() {
        return threadsAwake;
    }

    public synchronized void setThreadsSleeping(int i) {
        this.threadsSleeping = i;
        this.threadsAwake = NTEST - 0;
    }

    public synchronized void incrThreadsSleeping() {
        this.threadsSleeping++;
        this.threadsAwake--;
    }

    public synchronized void decrThreadsSleeping() {
        this.threadsSleeping--;
        this.threadsAwake++;
    }
}
