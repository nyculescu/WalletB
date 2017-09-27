package nyc.walletb;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 *
 */
public class MainFragment extends Fragment {
    final String TAG = getClass().getName();

    private FrameLayout fragmentContainer;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

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
    public static MainFragment newInstance(int index) {
        MainFragment fragment = new MainFragment();
        Bundle b = new Bundle();
        b.putInt("index", index);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        switch (getArguments().getInt("index", 0)) {
            case 0:
                view = inflater.inflate(R.layout.fragment_demo_settings, container, false);
                // initDemoSettings(view);
                return view;
            case 1:
                view = inflater.inflate(R.layout.fragment_split, container, false);
                initSplitPage(view);
                // initDemoSettings(view);
                return view;
            default:
                view = inflater.inflate(R.layout.fragment_demo_list, container, false);
                initDemoList(view);
                return view;
        }
    }

    /**
     * Initialization of the SPLIT Fragment
     */
    private void initSplitPage(final View view) {
        // Who bought? --section
        split_who_array = new ArrayList<>();
        split_who_array.add("Gogosica"); // TODO: 9/21/2017 to be removed and replaced with the list which will be got from user's config
        split_who_array.add("Gogosel"); //// TODO: 9/21/2017 the same as above
        split_who_et = (AutoCompleteTextView) view.findViewById(R.id.split_WhoBought_ETxt);
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

    /**
     * Init demo settings
     */
    private void initDemoSettings(View view) {

        final MainActivity mainActivity = (MainActivity) getActivity();
        final SwitchCompat switchColored = (SwitchCompat) view.findViewById(R.id.fragment_demo_switch_colored);
        final SwitchCompat switchFiveItems = (SwitchCompat) view.findViewById(R.id.fragment_demo_switch_five_items);
        final SwitchCompat showHideBottomNavigation = (SwitchCompat) view.findViewById(R.id.fragment_demo_show_hide);
        final SwitchCompat showSelectedBackground = (SwitchCompat) view.findViewById(R.id.fragment_demo_selected_background);
        final SwitchCompat switchForceTitleHide = (SwitchCompat) view.findViewById(R.id.fragment_demo_force_title_hide);
        final SwitchCompat switchTranslucentNavigation = (SwitchCompat) view.findViewById(R.id.fragment_demo_translucent_navigation);

        switchColored.setChecked(mainActivity.isBottomNavigationColored());
        switchFiveItems.setChecked(mainActivity.getBottomNavigationNbItems() == 5);
        switchTranslucentNavigation.setChecked(getActivity()
                .getSharedPreferences("shared", Context.MODE_PRIVATE)
                .getBoolean("translucentNavigation", false));
        switchTranslucentNavigation.setVisibility(
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? View.VISIBLE : View.GONE);

        switchTranslucentNavigation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getActivity()
                        .getSharedPreferences("shared", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("translucentNavigation", isChecked)
                        .apply();
                mainActivity.reload();
            }
        });
        switchColored.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // mainActivity.updateBottomNavigationColor(isChecked);
            }
        });
        switchFiveItems.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // mainActivity.updateBottomNavigationItems(isChecked);
            }
        });
        showHideBottomNavigation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // mainActivity.showOrHideBottomNavigation(isChecked);
            }
        });
        showSelectedBackground.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // mainActivity.updateSelectedBackgroundVisibility(isChecked);
            }
        });
        switchForceTitleHide.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // mainActivity.setForceTitleHide(isChecked);
            }
        });
    }

    /**
     * Init the fragment
     */
    private void initDemoList(View view) {

        fragmentContainer = (FrameLayout) view.findViewById(R.id.fragment_container);
        recyclerView = (RecyclerView) view.findViewById(R.id.fragment_demo_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        ArrayList<String> itemsData = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            itemsData.add("Fragment " + getArguments().getInt("index", -1) + " / Item " + i);
        }

        MainAdapter adapter = new MainAdapter(itemsData);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Refresh
     */
    public void refresh() {
        if (getArguments().getInt("index", 0) > 0 && recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    /**
     * Called when a fragment will be displayed
     */
    public void willBeDisplayed() {
        // Do what you want here, for example animate the content
        if (fragmentContainer != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.botnavi_fade_in);
            fragmentContainer.startAnimation(fadeIn);
        }
    }

    /**
     * Called when a fragment will be hidden
     */
    public void willBeHidden() {
        if (fragmentContainer != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.botnavi_fade_out);
            fragmentContainer.startAnimation(fadeOut);
        }
    }
}
