// Nicholas Rolland
// Assignment 3
// COP 4520, Spring 2024, Prof. Juan Parra

import java.util.*;

public class Problem1
{
    public static final int NUM_SERVANTS = 4;
    public static final int NUM_PRESENTS = 500000;
    public static final boolean DEBUG = true;

    public static void main(String[] args)
    {
        Presents presents = new Presents(NUM_PRESENTS);
        Servant[] servants = new Servant[NUM_SERVANTS];
        for (int i = 0; i < NUM_SERVANTS; i++)
        {
            servants[i] = new Servant(i);
            servants[i].start();
        }
        for (int i = 0; i < NUM_SERVANTS; i++)
        {
            try
            {
                servants[i].join();
            }
            catch (InterruptedException e)
            {}
        }
    }
}

// Stores the presents, whether in the bag or in the ordered chain.
class Presents
{
    public static Stack<Integer> bag;
    public static CLLnode chain;

    public Presents(int numPresents)
    {
        bag = new Stack<Integer>();
        int[] tempBag = new int[numPresents];
        for (int i = 0; i < numPresents; i++)
        {
            tempBag[i] = i + 1;
        }
        shuffle(tempBag);
        for (int i : tempBag)
            bag.push(i);

        chain = null;
    }

    // Fisher-Yates shuffle algorithm for shuffling unordered bag.
    public static void shuffle(int[] arr)
    {
        int index = 0, temp = 0;
        Random random = new Random();
        for (int i = arr.length - 1; i > 0; i--)
        {
            index = random.nextInt(i+1);
            temp = arr[index];
            arr[index] = arr[i];
            arr[i] = temp;
        }
    }

    // Not very efficient coarse-grained lock, but works and is easy to implement.
    public static synchronized void work(int id)
    {
        System.out.print("Servant " + id + " ");
        int action = (int)(Math.random() * 2) + 1;
        switch (action)
        {
            case 1:
                addPresent();
                break;
            case 2:
                writeCard();
                break;
            case 3:
                checkPresent((int)(Math.random() * Problem1.NUM_PRESENTS));
                break;
        }
    }

    // Action 1: Grab present from unordered bag, and add it to correct spot in chain.
    public static void addPresent()
    {
        if (bag.size() == 0) return;
        int present = bag.pop();

        // Start of chain.
        if (chain == null)
        {
            chain = new CLLnode(present);
            return;
        }
        CLLnode temp = chain;
        while (temp.next != null && temp.next.data > present)
        {
            temp = temp.next;
        }
        // Connect present and reform links
        CLLnode newPresent = new CLLnode(present);
        newPresent.next = temp.next;
        temp.next = newPresent;
        System.out.println("Added present " + present + " to chain");
    }

    // Action 2: Remove present from chain and write thank you card. To simplify, only pull from head of chain.
    public static void writeCard()
    {
        if (chain == null) return;
        CLLnode temp = chain;
        chain = temp.next;
        System.out.println("Wrote Thank you card to guest " + temp.data);
    }

    // Action 3: Check if a given present is in the chain.
    public static boolean checkPresent(int presentID)
    {
        if (chain == null) return false;
        CLLnode temp = chain;
        while (temp != null)
        {
            if (temp.data == presentID) return true;
        }
        return false;
    }
}

// Concurrent linked list node.
class CLLnode
{
    public int data;
    public CLLnode next;
    public CLLnode(int data)
    {
        this.data = data;
        this.next = null;
    }
}

class Servant extends Thread
{
    public int id;

    public Servant(int id)
    {
        this.id = id;
    }
    public void run()
    {
        while ((Presents.bag.size() > 0 || Presents.chain != null))
        {
            Presents.work(id);
        }
    }
}