package layout.main.Fragments;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import layout.main.Activities.MainActivity;
import nyc.walletb.R;

import static android.view.View.GONE;

public class UtilitiesFragment extends FragmentComplement {
    private final String TAG = getClass().getName();

    public UtilitiesFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        return new UtilitiesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_utilities, container, false);

        super.init(view, R.id.fragment_utilities_framelayout);

        final TextView textView = (TextView) view.findViewById(R.id.frg_utilities_what_txtv);
        final AutoCompleteTextView imageView = (AutoCompleteTextView) view.findViewById(R.id.frg_utilities_what_actxtv);

        return view;
    }
}
