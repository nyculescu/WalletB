package layout.main.Fragments;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import layout.main.Dialogs.DatePickerDialogFragment;
import nyc.walletb.R;
import nyc.walletb.SplitActivity_Template;

/**
 *
 */
public class SplitFragment extends FragmentComplement {
    private final String TAG = getClass().getName();

    // UI - Split fragment
    String[] split_autocomplete_fromwhere = {"Auchan", "Carrefour", "Profi", "Lidl", "Auchan Sud", "Auchan Nord", "Auchan Iulius Mall"};
    AutoCompleteTextView split_who_et;
    ArrayList<String> split_who_array;
    EditText split_calendar_et;
    EditText split_howmuch_et;
    Spinner split_currency_spinner;
    ArrayList<String> split_currency_array;
    AutoCompleteTextView split_fromwho_actv;

    /**
     * Create a new instance of the fragment
     */
    public static Fragment newInstance() {
        return new SplitFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_split, container, false);

        super.init(view, R.id.fragment_split_framelayout);

        initSplitFragment(view);

        return view;

    }

    /**
     * Initialization of the SPLIT Fragment
     */
    private void initSplitFragment(final View view) {
        // Who bought? --section
        split_who_array = new ArrayList<>();
        split_who_array.add("Gogosica"); // TODO: 9/21/2017 to be removed and replaced with the list which will be got from user's config
        split_who_array.add("Gogosel"); //// TODO: 9/21/2017 the same as above
        split_who_et = (AutoCompleteTextView) view.findViewById(R.id.split_WhoPaid_ETxt);
        ArrayAdapter<String> who_array_adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.select_dialog_singlechoice, split_who_array); //Create Array Adapter
        split_who_et.setThreshold(0); //Set the number of characters the user must type before the drop down list is shown
        split_who_et.setAdapter(who_array_adapter); //Set the adapter
        split_who_et.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!split_who_et.isPopupShowing()) {
                    split_who_et.showDropDown();
                }
                return false;
            }
        });

        // When? --section
        split_calendar_et = (EditText) view.findViewById(R.id.split_When_ETxt);
        split_calendar_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerDialogFragment();
                newFragment.show(getActivity().getFragmentManager(), "datePicker");
            }
        });
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat calendar_sdf = new SimpleDateFormat("dd.MM.yyyy");
        String calendar_formatted_date = calendar_sdf.format(calendar.getTime());
        split_calendar_et.setText(calendar_formatted_date); //default value: current date

        // From Where? --section
        split_fromwho_actv = (AutoCompleteTextView) view.findViewById(R.id.split_FromWhere_acTxtV);
        ArrayAdapter<String> adp_frmwh = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_dropdown_item_1line, split_autocomplete_fromwhere);
        split_fromwho_actv.setThreshold(0);
        split_fromwho_actv.setAdapter(adp_frmwh);
        split_fromwho_actv.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (!event.isShiftPressed()) {
                        // the user is done typing.
                        SPLIT_autocomplete_checkNewEntry(v.getText().toString());
                        return true; // consume.
                    }
                }
                return false;
            }
        });

        // How much? --section
        split_howmuch_et = (EditText) view.findViewById(R.id.split_HowMuch_ETxt);
        split_currency_array = new ArrayList<>();
        split_currency_array.add("Lei");
        split_currency_array.add("Euro"); //TODO: 9/21/2017 to be added more currencies (from a database)
        split_currency_spinner = (Spinner) view.findViewById(R.id.split_HowMuch_Spin);
        ArrayAdapter<String> currency_spinner_adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, split_currency_array);
        split_currency_spinner.setAdapter(currency_spinner_adapter); // this will set list of currencies to the split_currency_spinner
        split_currency_spinner.setSelection(split_currency_array.indexOf(0)); //set "Lei" as default
        /* //TODO: 9/21/2017 to be set the currency of the user's country
        Locale locale;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			locale = view.getContext().getResources().getConfiguration().getLocales().get(0);
		} else {
			locale = view.getContext().getResources().getConfiguration().locale;
		}
		Currency currency = Currency.getInstance(locale); */

        // OK button
        Button ok_button = (Button) view.findViewById(R.id.split_OK_btn);
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*--------------------- Collect data from user inputs ---------------------*/
                SplitActivity_Template collected_data = new SplitActivity_Template();

                collected_data.setWho_bought(split_who_et.getText().toString());

                try {
                    SimpleDateFormat date_format = new SimpleDateFormat("dd.mm.yyyy", Locale.ENGLISH);
                    collected_data.setWhen_was_it_bought(date_format.parse(split_calendar_et.getText().toString()));
                } catch (ParseException e) {
                    collected_data.setWhen_was_it_bought(new Date());
                    e.printStackTrace();
                }

                collected_data.setFrom_where_was_it_bought(split_fromwho_actv.getText().toString());

                try {
                    collected_data.setHow_much_did_it_cost(Float.valueOf(split_howmuch_et.getText().toString()));
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.getMessage());
                }

                /*------------------------- Save this data locally -------------------------*/
                /* The data which is not synchronized will be saved temporary in sharedpreferences
                 *
                 * For debug (NON-ROOTED phones) I used  http://facebook.github.io/stetho/
                 * 1) in build.gradle add compile 'com.facebook.stetho:stetho:1.5.0'
                 * 2) in the onCreate() add Stetho.initializeWithDefaults(this);
                 * 3) in Chrome, from PC, go to the chrome://inspect/
                 * 4) in Chrome go to Remote Target > nyc.walletb > inspect >> Local Storage > nyc.walletb_preferences
                 */
                // Write into SharedPreferences
                SharedPreferences sharedPrefs_W = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor_W = sharedPrefs_W.edit();
                Gson gson_W = new Gson();
                String json_W = gson_W.toJson(collected_data.allParamsToString());
                editor_W.putString(TAG, json_W);
                editor_W.apply();

                // Read from SharedPreferences TODO shall be moved from here!
                /*
                SharedPreferences sharedPrefs_R = PreferenceManager.getDefaultSharedPreferences(getContext());
                Gson gson_R = new Gson();
                String json_R = sharedPrefs_R.getString(TAG, null);
                Type type = new TypeToken<ArrayList<String>>() {}.getType();
                ArrayList<String> dataFromSharePref = gson_R.fromJson(json_R, type);
                */

                /*----- If internet is available, send the data to Google Spreadsheet -----*/
                // TODO: 9/25/2017 implement this feature
                /* Data will be stored in a Google Spread Sheet. Using databases in this app will require servers, which cost a lot of money :)
                 * TODO implementation of a database instead Google Spread Sheet
                 * */

            }
        });
    }

    // TODO: 26.08.2017 dummy - should be implemented
    private void SPLIT_autocomplete_checkNewEntry(String text) {
        // https://stackoverflow.com/questions/7899525/how-to-split-a-string-by-space
        String[] splitted = text.split("\\s+");

        // https://stackoverflow.com/questions/7552253/how-to-remove-special-characters-from-a-string
        String[] splitted_spchrem = new String[splitted.length];
        for (int i = 0; i < splitted.length; i++)
            splitted_spchrem[i] = splitted[i].replaceAll("[-+.^:,]", "");
    }
}
