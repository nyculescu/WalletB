package nyc.walletb;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 */
public class MainFragment extends Fragment {

	private FrameLayout fragmentContainer;
	private RecyclerView recyclerView;
	private RecyclerView.LayoutManager layoutManager;

	// UI - Split fragment
	String SPLIT_autocomplete_fromwhere[]={"Auchan","Carrefour", "Profi", "Lidl", "Auchan Sud", "Auchan Nord", "Auchan Iulius Mall"};
    EditText et_who;
    EditText et_cal;
    EditText et_howmuch;
    AutoCompleteTextView actv_frmwh;

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
		switch (getArguments().getInt("index", 0)){
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

	private void initSplitPage(final View view) {
        // Who bought? --section
        et_who = (EditText) view.findViewById(R.id.split_WhoBought_ETxt);

        // When? --section
        et_cal = (EditText) view.findViewById(R.id.split_When_ETxt);
        et_cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getActivity().getFragmentManager(), "datePicker");
            }
        });

        // From Where? --section
		actv_frmwh = (AutoCompleteTextView) view.findViewById(R.id.split_FromWhere_acTxtV);
		ArrayAdapter<String> adp_frmwh = new ArrayAdapter<>(view.getContext(),	android.R.layout.simple_dropdown_item_1line, SPLIT_autocomplete_fromwhere);
        actv_frmwh.setThreshold(1);
        actv_frmwh.setAdapter(adp_frmwh);
        actv_frmwh.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
        et_howmuch = (EditText) view.findViewById(R.id.split_HowMuch_ETxt);

        // OK button
        Button ok_button = (Button) view.findViewById(R.id.split_OK_btn);
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String who_bought = et_who.getText().toString();

                String date_pattern = "dd.mm.yyyy";
                try {
                    Date when = new SimpleDateFormat(date_pattern).parse(et_cal.getText().toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String from_where = actv_frmwh.getText().toString();

                float how_much = Float.valueOf(et_howmuch.getText().toString());


            }
        });
	}

	// TODO: 26.08.2017 dummy - should be implemented
    private void SPLIT_autocomplete_checkNewEntry(String text) {
		// https://stackoverflow.com/questions/7899525/how-to-split-a-string-by-space
		String[] splitted = text.split("\\s+");

		// https://stackoverflow.com/questions/7552253/how-to-remove-special-characters-from-a-string
		String[] splitted_spchrem = new String[splitted.length];
		for (int i=0; i<splitted.length; i++)
			splitted_spchrem[i] = splitted[i].replaceAll("[-+.^:,]","");
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
