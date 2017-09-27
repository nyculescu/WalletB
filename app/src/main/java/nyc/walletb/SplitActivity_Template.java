package nyc.walletb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by nyc-PC on 9/26/2017.
 */

public class SplitActivity_Template {
    public String getWho_bought() {
        return who_bought;
    }

    public void setWho_bought(String who_bought) {
        this.who_bought = who_bought;
    }

    public Date getWhen_was_it_bought() {
        return when_was_it_bought;
    }

    public void setWhen_was_it_bought(Date when_was_it_bought) {
        this.when_was_it_bought = when_was_it_bought;
    }

    public String getFrom_where_was_it_bought() {
        return from_where_was_it_bought;
    }

    public void setFrom_where_was_it_bought(String from_where_was_it_bought) {
        this.from_where_was_it_bought = from_where_was_it_bought;
    }

    public float getHow_much_did_it_cost() {
        return how_much_did_it_cost;
    }

    public void setHow_much_did_it_cost(float how_much_did_it_cost) {
        this.how_much_did_it_cost = how_much_did_it_cost;
    }

    private String who_bought = "";
    private Date when_was_it_bought = new Date();
    private String from_where_was_it_bought = "";
    private float how_much_did_it_cost = 0;

    public SplitActivity_Template(String who_bought, Date when_was_it_bought, String from_where_was_it_bought, float how_much_did_it_cost) {
        this.who_bought = who_bought;
        this.when_was_it_bought = when_was_it_bought;
        this.from_where_was_it_bought = from_where_was_it_bought;
        this.how_much_did_it_cost = how_much_did_it_cost;
    }

    public SplitActivity_Template(){}

    /**
     * Returns all parameters as a list of Strings
     * */
    public List<String> allParamsToString(){
        List<String> list_to_return = new ArrayList<>();
        list_to_return.add(this.who_bought);
        list_to_return.add("" + this.when_was_it_bought);
        list_to_return.add(this.from_where_was_it_bought);
        list_to_return.add("" + this.how_much_did_it_cost);

        return list_to_return;
    }
}
