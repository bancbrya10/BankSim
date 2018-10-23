/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.temple.cis.c3238.banksim;

/**
 *
 * @author Bryan
 */
public class Sum extends Thread {

    private final Bank bank;

    public Sum(Bank b) {
        bank = b;
    }

    @Override
    public synchronized void run() {
        bank.test();
    }

}
