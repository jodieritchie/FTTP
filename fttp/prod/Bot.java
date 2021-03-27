import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
 


public class Bot {

    public static void main(String[] args) {
        /* The boolean passed to the Configuration constructor dictates whether or not the
           bot is connecting to the prod or test exchange. Be careful with this switch! */
        Configuration config = new Configuration(false);
        try {
            Socket skt = new Socket(config.exchange_name(), config.port());
            BufferedReader from_exchange = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            PrintWriter to_exchange = new PrintWriter(skt.getOutputStream(), true);

            /*
              A common mistake people make is to to_exchange.println() > 1
              time for every from_exchange.readLine() response.
              Since many write messages generate marketdata, this will cause an
              exponential explosion in pending messages. Please, don't do that!
            */
            to_exchange.println(("HELLO " + config.team_name).toUpperCase());
            String reply = from_exchange.readLine().trim();
            System.err.printf("The exchange replied: %s\n", reply);
            int order_id = 0;
            Queue<Integer[]> VALBZbuyQueue = new LinkedList<>();
            Queue<Integer[]> VALBZsellQueue = new LinkedList<>();
            int VALBZpos = 0;
            while (true) {
                String message[] = from_exchange.readLine().trim().split(" ");
                for (String s : message) {
                    System.out.print(s + " ");
                }
                System.out.println(" ");
                if (message[0].equals("CLOSE")) {
                    System.out.println("The round has ended");
                    break;
                } else if (message[0].equals("BOOK") && message[1].equals("BOND")) {
                    int index = 3;
                    // Convention for putting orders in: int[price, size]
                    int price, size;
                    int minBuy = 970;
                    int maxSell = 1030; 
                    while(!message[index].equals("SELL")) {
                        String trade[] = message[index].trim().split(":");
                        price = Integer.parseInt(trade[0]);
                        size = Integer.parseInt(trade[1]);
                        if (price > 1000) {
                            to_exchange.println("ADD " + order_id + " BOND SELL " + price + " " + size);
                            System.out.println("ADD " + order_id + " BOND SELL " + price + " " + size);
                            order_id++;
                            if (price > maxSell) {
                                maxSell = price;
                            }
                        }
                        index++;
                    }
                    index++;
                    while(index < message.length) {
                        String[] trade = message[index].trim().split(":");
                        price = Integer.parseInt(trade[0]);
                        size = Integer.parseInt(trade[1]);
                        if (price < 1000) {
                            to_exchange.println("ADD " + order_id + " BOND BUY " + price + " " + size);
                            System.out.println("ADD " + order_id + " BOND BUY " + price + " " + size);
                            order_id++;
                            if (price < minBuy) {
                                minBuy = price;
                            }
                        }
                        index++;
                    }
                    if (minBuy > 980) {
                        to_exchange.println("ADD " + order_id + " BOND BUY " + minBuy  + " " + 10);
                        System.out.println("ADD " + order_id + " BOND BUY " + minBuy  + " " + 10);
                        order_id++;
                    }
                    if (maxSell < 1020) {
                        to_exchange.println("ADD " + order_id + " BOND SELL " + maxSell + " " + 10);
                        System.out.println("ADD " + order_id + " BOND SELL " + maxSell + " " + 10);
                        order_id++;
                    }
                } 
                else if (message[0].equals("BOOK") && message[1].equals("VALBZ")) {
                    int index = 3;
                    // Convention for putting orders in: int[price, size]
                    VALBZbuyQueue = new LinkedList<>();
                    VALBZsellQueue = new LinkedList<>();
                    while(!message[index].equals("SELL")) {
                        String trade[] = message[index].trim().split(":");
                        Integer[] arr = new Integer[2];
                        arr[0] = Integer.parseInt(trade[0]);
                        arr[1] = Integer.parseInt(trade[1]);
                        VALBZbuyQueue.add(arr);
                        index++;
                    }
                    index++;
                    while(index < message.length) {
                        String[] trade = message[index].trim().split(":");
                        Integer[] arr = new Integer[]{Integer.parseInt(trade[0]), Integer.parseInt(trade[1])};
                        VALBZsellQueue.add(arr);
                        index++;
                    }
                    
                }
                else if (message[0].equals("BOOK") && message[1].equals("VALE")) {
                    int index = 3;
                    // Convention for putting orders in: int[price, size]
                    int price, size;
               
                    while(!message[index].equals("SELL")) {
                        String trade[] = message[index].trim().split(":");
                        price = Integer.parseInt(trade[0]);
                        size = Integer.parseInt(trade[1]);
                        Integer[] VALBZbuy = VALBZsellQueue.peek();
                        if(VALBZbuy == null){
                            index++;
                            continue;
                        }
                        int VALBZprice = VALBZbuy[0];
                        int VALBZsize = VALBZbuy[1];
                        if(price > VALBZprice && size > VALBZsize) {
                            if(VALBZpos + VALBZsize > 10){
                                int diff = VALBZpos + VALBZsize - 10;
                                to_exchange.println("ADD " + order_id + " VALE SELL " + price + " " + (int)(VALBZsize - diff));
                                to_exchange.println("ADD " + (int)(order_id + 1) + " VALBZ BUY " + VALBZprice + " " + (int)(VALBZsize - diff));
                                to_exchange.println("CONVERT " + (int)(order_id + 2) + " VALE BUY 10");
                                order_id+=2;
                                VALBZsize = diff;
                                VALBZpos = diff;
                            }else{
                                VALBZpos += VALBZsize;
                            }
                            to_exchange.println("ADD " + order_id + " VALE SELL " + price + " " + VALBZsize);
                            to_exchange.println("ADD " + (int)(order_id + 1) + " VALBZ BUY " + VALBZprice + " " + VALBZsize);
                            // System.out.println("ADD " + order_id + " VALE SELL " + price + " " + VALBZsize);
                            // System.out.println("ADD " + (int)(order_id + 1) + " VALBZ BUY " + VALBZprice + " " + VALBZsize);
                            VALBZsellQueue.remove();
                            order_id+=2;
                        }else if(price > VALBZprice && size <= VALBZsize) {
                            Integer[] arr = new Integer[2];
                            arr[0] = VALBZprice;
                            arr[1] = VALBZsize - size;

                            if(VALBZpos + size > 10){
                                int diff = VALBZpos + size - 10;
                                to_exchange.println("ADD " + order_id + " VALE SELL " + price + " " + (int)(size - diff));
                                to_exchange.println("ADD " + (int)(order_id + 1) + " VALBZ BUY " + VALBZprice + " " + (int)(size - diff));
                                to_exchange.println("CONVERT " + (int)(order_id + 2) + " VALE BUY 10");
                                order_id+=2;
                                VALBZpos = diff;
                                size = diff;
                            }else{
                                VALBZpos += size;
                            }
                        	to_exchange.println("ADD " + order_id + " VALE SELL " + price + " " + size);
                            to_exchange.println("ADD " + (int)(order_id + 1) + " VALBZ BUY " + VALBZprice + " " + size);
                            // System.out.println("ADD " + order_id + " VALE SELL " + price + " " + size);
                            // System.out.println("ADD " + (int)(order_id + 1) + " VALBZ BUY " + VALBZprice + " " + size);

                        	
                            VALBZsellQueue.remove();
                            VALBZsellQueue.add(arr);
                            
                            order_id+=2;
                        }
                        index++;
                    }
                    index++;
                    while(index < message.length) {
                        String trade[] = message[index].trim().split(":");
                        price = Integer.parseInt(trade[0]);
                        size = Integer.parseInt(trade[1]);
                        Integer[] VALBZsell = VALBZbuyQueue.peek();
                        if(VALBZsell == null){
                            index++;
                            continue;
                        }
                        int VALBZprice = VALBZsell[0];
                        int VALBZsize = VALBZsell[1];
                        if(price < VALBZprice && size > VALBZsize) {
                            if(VALBZpos - VALBZsize < -10){
                                int diff = -(VALBZpos - VALBZsize + 10);
                                to_exchange.println("ADD " + order_id + " VALE BUY " + price + " " + (int)(VALBZsize - diff));
                                to_exchange.println("ADD " + (int)(order_id + 1) + " VALBZ SELL " + VALBZprice + " " + (int)(VALBZsize - diff));
                                to_exchange.println("CONVERT " + (int)(order_id + 2) + " VALE SELL 10");
                                order_id+=2;
                                VALBZpos = -diff;
                                VALBZsize = diff;
                            }else{
                                VALBZpos -= VALBZsize;
                            }

                            to_exchange.println("ADD " + order_id + " VALE BUY " + price + " " + VALBZsize);
                            to_exchange.println("ADD " + (int)(order_id + 1) + " VALBZ SELL " + VALBZprice + " " + VALBZsize);
                            // System.out.println("ADD " + order_id + " VALE BUY " + price + " " + VALBZsize);
                            // System.out.println("ADD " + (int)(order_id + 1) + " VALBZ SELL " + VALBZprice + " " + VALBZsize);
                            VALBZbuyQueue.remove();
                        }else if(price < VALBZprice && size <= VALBZsize) {
                            if(VALBZpos - size < -10){
                                int diff = -(VALBZpos - size + 10);
                                to_exchange.println("ADD " + order_id + " VALE BUY " + price + " " + (int)(size - diff));
                                to_exchange.println("ADD " + (int)(order_id + 1) + " VALBZ SELL " + VALBZprice + " " + (int)(size - diff));
                                to_exchange.println("CONVERT " + (int)(order_id + 2) + " VALE SELL 10");
                                order_id+=2;
                                VALBZpos = -diff;
                                size = diff;
                            }else{
                                VALBZpos -= size;
                            }
                        	to_exchange.println("ADD " + order_id + " VALE BUY " + price + " " + size);
                            to_exchange.println("ADD " + (int)(order_id + 1) + " VALBZ SELL " + VALBZprice + " " + size);
                            // System.out.println("ADD " + order_id + " VALE BUY " + price + " " + size);
                            // System.out.println("ADD " + (int)(order_id + 1) + " VALBZ SELL " + VALBZprice + " " + size);
                        	Integer[] arr = new Integer[2];
                        	arr[0] = VALBZprice;
                            arr[1] = VALBZsize - size;
                            VALBZbuyQueue.remove();
                            VALBZbuyQueue.add(arr);
                        }
                        order_id+=2;
                        index++;
                    }
                }
                // else if (message[0].equals("BOOK") && message[1].equals("MS")) {
                //     int index = 3;
                //     // Convention for putting orders in: int[price, size]
                //     Queue<Integer[]> buyQueue = new LinkedList<>();
                //     while(!message[index].equals("SELL")) {
                //         String trade[] = message[index].trim().split(":");
                //         int[] arr = new arr[2];
                //         arr[0] = Integer.parseInt(trade[0]);
                //         arr[1] = Integer.parseInt(trade[1]);
                //         buyQueue.insert(arr);
                //     }
                //     while(index < message.length) {
                //         String[] trade = message[index].trim().split(":");
                //         int[] arr = new arr[]{Integer.parseInt(message[0]), Integer.parseInt(message[1])};
                //         if (arr[0] > 1000) {

                //         }
                //         index++;
                //     }
                // }
                // else if(message[0].equals("TRADE")) {
                    
                // }
                
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
