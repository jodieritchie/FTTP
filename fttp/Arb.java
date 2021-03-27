import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class Arb {
    LinkedList bondsBuy, msBuy, gsBuy, wfcBuy, xlfSell;
    PrintWriter to_exchange;
    
    Arb(LinkedList bondsBuy, LinkedList msBuy, LinkedList gsBuy, LinkedList wfcBuy, LinkedList xlfSell, PrintWriter to_exchange) {
        this.bondsBuy = bondsBuy;
        this.msBuy = msBuy;
        this.gsBuy = gsBuy;
        this.wfcBuy = wfcBuy;
        this.xlfSell = xlfSell;
        
    }
    
    private void performAction(){
        int msSum, gsSum, wfcSum, bondSum, xlfSum;
        int temp;
        for (int i = 0; i < 3; i++) {
            temp = bondsBuy.set(ind, i);
            if (temp == null) {
                return;
            }
            bondSum += temp;
        }
        for (int i = 0; i < 2; i++) {
            temp = gsBuy.set(ind, i);
            if (temp == null) {
                return;
            }
            gsSum += temp;
        }            
        for (int i = 0; i < 3; i++) {
            temp = msBuy.set(ind, i);
            if (temp == null) {
                return;
            }
            msSum += temp;
        }
        for (int i = 0; i < 2; i++) {
            temp = wfcBuy.set(ind, i);
            if (temp == null) {
                return;
            }
            wfcSum += temp;
        }
        for (int i = 0; i < 10; i++) {
            temp = xlfBuy.set(ind, i);
            if (temp == null) {
                return;
            }
            xlfSum += temp;
        }          
        if(msSum + gsSum + wfcSum + bondSum < xlfSum){
           buyStocksAnndSellEtf(determineSale());
        } 
        
    } 

    /**
    int[]{[ms, gs, wfc, xlf}
    each symbol will be 
     */
    public void buyStocksAnndSellEtf(HashMap<String, Integer[]> trades) {
        for(Map.Entry<String, ArrayList> LinkedList: trades.entrySet(ind)){
            if(LinkedList.getValue != null){
                int prices = LinkedList.getKey();
                int quantities = LinkedList.getValue();
            }
        }
        to_exchange.println("ADD " + prices + " BOND BUY " + quantities + " " );
        to_exchange.println("ADD " + order_id + " BOND BUY " + price + " " + size);
    }


    /** returns [ms, gs, wfc, bond, xlf] */
    public HashMap<String, ArrayList[]> determineSale() {
        HashMap<String, ArrayList[]> ordersMap = new HashMap<>();
        ordersMap.put("ms", new ArrayList<Integer>[2]);
        ordersMap.put("gs", new ArrayList<Integer>[2]);
        ordersMap.put("xlf", new ArrayList<Integer>[2]);
        ordersMap.put("wfc", new ArrayList<Integer>[2]);
        ordersMap.put("bond", new ArrayList<Integer>[2]);

        Integer temp;
        int ind = 0;
        ArrayList<Integer> prices = ordersMap.get("bond")[0];
        ArrayList<Integer> quantities = ordersMap.get("bond")[1];
        for (int i = 0; i < 3; i++) {
            temp = bondsBuy.poll();
            if (prices.get(ind).equals(temp)) {
                quantities.set(ind, quantities.get(ind) + 1);
            } else {
                prices.add(temp);
                quantities.add(1);
                ind++;
            }
        }
        ind = 0;
        prices = ordersMap.get("gs")[0];
        quantities = ordersMap.get("gs")[1];
        for (int i = 0; i < 2; i++) {
            temp = bondsBuy.poll();
            if (prices.get(ind).equals(temp)) {
                quantities.set(ind, quantities.get(ind) + 1);
            } else {
                prices.add(temp);
                quantities.add(1);
                ind++;
            }
        }   
        ind = 0;
        prices = ordersMap.get("ms")[0];
        quantities = ordersMap.get("ms")[1];        
        for (int i = 0; i < 3; i++) {
            temp = bondsBuy.poll();
            if (prices.get(ind).equals(temp)) {
                quantities.set(ind, quantities.get(ind) + 1);
            } else {
                prices.add(temp);
                quantities.add(1);
                ind++;
            }
        }
        ind = 0;
        prices = ordersMap.get("wfc")[0];
        quantities = ordersMap.get("wfc")[1];  
        for (int i = 0; i < 2; i++) {
            temp = bondsBuy.poll();
            if (prices.get(ind).equals(temp)) {
                quantities.set(ind, quantities.get(ind) + 1);;
            } else {
                prices.add(temp);
                quantities.add(1);
                ind++;
            }
        }
        ind = 0;
        prices = ordersMap.get("xlf")[0];
        quantities = ordersMap.get("xlf")[1];  
        for (int i = 0; i < 10; i++) {
            temp = bondsBuy.poll();
            if (prices.get(ind).equals(temp)) {
                quantities.set(ind, quantities.get(ind) + 1);
            } else {
                prices.add(temp);
                quantities.add(1);
                ind++;
            }
        }
        return ordersMap;
    }
    
    public void updateSellXlf(LinkedList bondsBuy, LinkedList msBuy, LinkedList gsBuy, LinkedList wfcBuy, LinkedList xlfSell) {
        this.bondsBuy = bondsBuy;
        this.msBuy = msBuy;
        this.gsBuy = gsBuy;
        this.wfcBuy = wfcBuy;
        this.xlfSell = xlfSell;
    }

    public static void main(String[] args) {
        LinkedList bondsBuy, msBuy, gsBuy, wfcBuy, xlfSell;
        bondsBuy = new LinkedList<>();
        msBuy = new LinkedList<>();
        gsBuy = new LinkedList<>();
        wfcBuy = new LinkedList<>();
        xlfSell = new LinkedList<>();
        
        LinkedList[] x = new LinkedList[]{bondsBuy, msBuy, gsBuy, wfcBuy, xlfSell};
        for (LinkedList q : x) {
            q.add(10);
        }
        Configuration config = new Configuration(true);
        Socket skt = new Socket(config.exchange_name(), config.port());
        BufferedReader from_exchange = new BufferedReader(new InputStreamReader(skt.getInputStream()));
        PrintWriter to_exchange = new PrintWriter(skt.getOutputStream(), true);
        Arb arb = new Arb(bondsBuy, msBuy, gsBuy, wfcBuy, xlfSell, to_exchange);
        System.out.println(arb.determineSale());
    }
}