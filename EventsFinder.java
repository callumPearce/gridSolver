import java.util.ArrayList;
import java.util.Random;
import java.util.List;
import java.util.Collections;
import java.util.LinkedList;
import java.lang.Math.*;
import java.util.Scanner;


/* ------------------------------ EventsFinder Class ------------------------------- */
public class EventsFinder{

    public static void main(String[] args) {

        //1) Check if the correct number of arguments have been supplied
        if(args.length != 2) {
            System.err.println("Incorrect number of arguments supplied.");
            System.exit(1);
        }
        else if(Integer.parseInt(args[0])%2 == 0){
            System.err.println("The dimensions of the board must be an odd number.");
            System.exit(1);
        }

        //2) Initialise and parse command line arguments
        Integer y = 0;
        Integer x = 0;
        Integer yLen = 0;
        Integer xLen = 0;
        Integer eventsCount = 0;

        try {
            yLen = Integer.parseInt(args[0]);
            xLen = Integer.parseInt(args[0]);
            eventsCount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e){
            System.err.println("Arguments must all be integers.");
            System.exit(1);
        }

        //3) Get the location the user wishes find events from
        Scanner sc = new Scanner(System.in);
        System.out.println("Please input the coordinates you wish to search for in the following format x,y (e.g. 4,2):");
        String line;
        String[] lineValues;
        line = sc.nextLine();
        lineValues = line.split(",");
        x = Integer.parseInt(lineValues[0]);
        y = Integer.parseInt(lineValues[1]);


        //4) Convert to internal grid scale (e.g. for a scale of -10 to 10,
        // internal representation has scale 0 - 20 to index list correctly.
        y = y + (int) Math.floor(xLen/2);
        x = x + (int) Math.floor(yLen/2);

        if(x > xLen || y > yLen || x < 0 || y < 0) {
            System.err.println("Input coordinates cannot be found on the grid.");
            System.exit(1);
        }

        //5) Initialise the grid with the passed in xLen and yLen scale and display the generated world
        Grid grid = new Grid(xLen, yLen);
        grid.printWorld(x, y);

        //6) Find the closest eventsCount events and print the required data
        Node s = grid.world.get(x).get(y);
        List<Node> closestEvents = findClosestEvents(s, grid, eventsCount);
        printClosestEvents(s, grid, closestEvents);
    }


    //Finds the n closest nodes with events to the starting coordinates specified in the grid
    public static List<Node> findClosestEvents( Node s, Grid grid, Integer n ){

        //Define the queue for keeping track of discovered points
        LinkedList<Node> Q = new LinkedList<Node>();

        //Define the list of closest events
        List<Node> closestEvents = new ArrayList<Node>();

        //The initial node has been visited and enqueue it
        s.visited = true;
        Q.addFirst(s);

        //While the queue is not empty
        while(Q.peek() != null && (closestEvents.size() <= n)){

            Node v = Q.removeFirst();


            //If the node has an event add it add it, then check if n events have been found
            if(v.hasEvent()){
                closestEvents.add(v);
                if(closestEvents.size() == n)
                    break;
            }

            //Add neighbours who have not yet been visited to the queue
            List<Node> neighbours = grid.getNeighbours(v);

            for(Node w: neighbours){
                if (!w.visited){
                    w.visited = true;
                    Q.add(w);
                }
            }
        }

        return closestEvents;
    }


    //Prints out the found events with their id, cheapest ticket price and distance from the starting node
    public static void printClosestEvents(Node s, Grid grid, List<Node> closestEvents){

        //Calculate the distance between the node and the starting node
        for(Node n: closestEvents) {
            Integer distance = s.manhattanDistance(n);

            //Convert the cost into dollars
            Integer cost = n.event.tickets.get(0);
            String cents = Integer.toString(cost).substring(0, 2);
            String dollars = Integer.toString(cost).substring(2, 4); //Take seond part of cost to be dollars to increase the range in which ticket prices take.

            System.out.println("Event " + Integer.toString(n.event.id) + " - $" + dollars + "." + cents + ", Distance " + Integer.toString(distance));
        }
    }

}


/* ------------------------------ Grid Class ------------------------------- */
// A grid holds a collection of nodes to form the world.
class Grid {

    Integer xLen = 0;                   //x length of the grid
    Integer yLen = 0;                   //y length of the grid
    public List<List<Node>> world;      //the world of events

    //Construct a new grid with the specified number of rows and columns
    public Grid(Integer rows, Integer columns){

        this.xLen = columns;
        this.yLen = rows;

        //Initialise the world
        this.world = new ArrayList<List<Node>>();
        for(int i = 0; i < this.yLen; i++){
            this.world.add(new ArrayList<Node>());
        }


        populateWorld();
    }


    //Populate the world with events
    private void populateWorld(){
        //For every point on the world
        for(int y = 0; y < this.yLen; y++){
            for(int x = 0; x < this.xLen; x++){

                //Create a new node at that point of the world with an event
                Integer id = y + this.xLen*x;
                Node node = new Node(x,y);

                //Create a random integer between 1-100
                Random rand = new Random();
                Integer  rand_int = rand.nextInt(100) + 1;

                //15% Chance to create an event on a given node
                if(rand_int <= 15) {
                    Event event = new Event(id, 500, rand_int * 10000, 1000);
                    node.addEvent(event);
                }

                this.world.get(x).add(node);
            }
        }
    }


    //Given a node get its 4 neighbours (Manhatten distance approah)
    public List<Node> getNeighbours(Node node){

        List<Node> neighbours = new ArrayList<Node>();

        if(node.x + 1 < this.xLen)
            neighbours.add(this.world.get(node.x + 1).get(node.y));

        if(node.x - 1 >= 0)
            neighbours.add(this.world.get(node.x - 1).get(node.y));

        if(node.y + 1 < this.yLen)
            neighbours.add(this.world.get(node.x).get(node.y + 1));

        if(node.y - 1 >= 0)
            neighbours.add(this.world.get(node.x).get(node.y - 1));

        return neighbours;
    }


    //Print the world
    public void printWorld(Integer x_p, Integer y_p){
        System.out.println("");
        System.out.println("Generated world where...");
        System.out.println(".e marks an event within the grid");
        System.out.println(".o marks a position in the grid without an event");
        System.out.println(".? is the point you have inputted");
        System.out.println("");

        for(int y = 0; y < this.yLen; y++){
            for(int x = 0; x < this.xLen; x++){

                if(x == x_p && y == y_p)
                    System.out.print("? ");
                else if(this.world.get(x).get(y).hasEvent())
                    System.out.print("e ");
                else
                    System.out.print("o ");
            }
            System.out.println("");
        }
        System.out.println("");
    }

}


/* ------------------------------ Event Class ------------------------------- */
//An Event holds tickets and has a unique identifier.
class Event {

    Integer id = -1;                                           //Event identifier
    Integer minCost = 0;                                       //Minimum cost for an events tickets
    Integer maxCost = 0;                                       //Maximum cost for an events tickets
    Integer ticketCount = 0;                                   //Number of tickets for the event
    public List<Integer> tickets = new ArrayList<Integer>();   //List of tickets for the event


    //Construct a new event with the specified number of tickets given the events id, max cost and new cost
    public Event(Integer identifier, Integer tcount, Integer max, Integer min) {

        this.id = identifier;
        this.ticketCount = tcount;

        //Ensure max is not less then min
        if (max <= min) {
            this.minCost = min;
            this.maxCost = min;
        } else {
            this.minCost = min;
            this.maxCost = max;
        }

        //tickets = generateTickets(this.id, this.minCost, this.maxCost, this.ticketCount);
        generateTickets();

    }


    //Generates a list of tickets from the events fields
    private void generateTickets(){

        //Generate the speicified number of tickets within the specified range
        for(int i = 0; i < this.ticketCount; i++){
            Random r = new Random();

            int ticket_price = r.nextInt((this.maxCost - this.minCost) + 1) + this.minCost;
            (this.tickets).add(ticket_price);
        }

        //Sort the ticket list so the smallest ticket is first in the list
        Collections.sort(this.tickets);
    }

}


/* ------------------------------ Node Class ------------------------------- */
//A node can hold an event and is a specific location in the world.
//Class for a node within a graph
class Node{

    Integer x = 0;                  //x coordinate position
    Integer y = 0;                  //y coordinate position
    Event event = null;             //Event held by the node
    public boolean visited = false; //Visited marker to check if the node has been explored


    //Construct a node at a specific coordinate
    public Node(Integer xCord, Integer yCord){
        this.x = xCord;
        this.y = yCord;
    }


    //Add a new event to the node
    public void addEvent(Event new_event){
        this.event = new_event;
    }


    //Check if the node has an event
    public boolean hasEvent(){
        if (this.event == null){
            return false;
        } else{
          return true;
        }
    }


    //Calculate the manhattan distance between the node and the node passed in
    public Integer manhattanDistance(Node n){
        return Math.abs(this.x - n.x) + Math.abs(this.y - n.y);
    }
}