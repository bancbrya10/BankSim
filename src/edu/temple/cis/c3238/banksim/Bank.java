package edu.temple.cis.c3238.banksim;

/**
 * @author Cay Horstmann
 * @author Modified by Paul Wolfgang
 * @author Modified by Charles Wang
 */
public class Bank {

    public static final int NTEST = 10;
    private final Account[] accounts;
    private long ntransacts;
    private final int initialBalance;
    private final int numAccounts;

    private boolean waitForTest = false;
    private int threadsSleeping = 0;
    private int threadsAwake = 10;
    private Object lock = new Object();

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
            System.out.println("transfer has occured");
        }

        if (shouldTest()) {
            waitForTest = true;
            System.out.println("waitForTest set to TRUE");
            System.out.println(ntransacts);
        }

        if (waitForTest) {
        // If test() needs to be ran (waitForTest set to true) this method will
        // put each thread to sleep and track threadsPutToSleep/Awake
        sleepTransferThread();
        //System.out.println("waitForTest " + waitForTest);
        //long threadId = Thread.currentThread().getId();
        //System.out.println("thread " + threadId + " back in service");
        }
    }

    public void test() {
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

    }

    public synchronized void sleepTransferThread() {

        //System.out.println("threads awake " + this.getThreadsAwake());

        //Last thread to sleep creates new thread to run test
        if (this.getThreadsAwake() == 1) {
            Sum sum = new Sum(this);
            sum.start();
        }

        //Thread is about to sleep, adjust count
        this.incrThreadsSleeping();
        
        //System.out.println("sleep " + Thread.currentThread().toString());
        while (waitForTest) {
            try {
                this.wait();
            } catch (InterruptedException ex) {
            }
        }
        

        //Thread has resumed running, adjust count
        this.decrThreadsSleeping();
        //System.out.println("awake  " + Thread.currentThread().toString());

    }

    public int getThreadsSleeping() {
        return threadsSleeping;
    }

    public int getThreadsAwake() {
        return threadsAwake;
    }

    public void setThreadsSleeping(int i) {
        this.threadsSleeping = i;
        this.threadsAwake = NTEST - 0;
    }

    public void incrThreadsSleeping() {
        this.threadsSleeping++;
        this.threadsAwake--;
    }

    public void decrThreadsSleeping() {
        this.threadsSleeping--;
        this.threadsAwake++;
    }

    public long incrNtransacts() {
        this.ntransacts++;
        return ntransacts;
    }

    public int size() {
        return accounts.length;
    }
    
    public void setFlagFalse(){
        this.waitForTest = false;
    }

    public boolean shouldTest() {
        return incrNtransacts() % NTEST == 0;
    }

}
