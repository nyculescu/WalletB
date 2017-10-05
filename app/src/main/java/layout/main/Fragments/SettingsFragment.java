package layout.main.Fragments;

import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import layout.main.Activities.MainActivity;
import nyc.walletb.R;

import static android.view.View.GONE;

public class SettingsFragment extends FragmentComplement {

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        super.init(view, R.id.fragment_settings_framelayout);

        final Button button = (Button) view.findViewById(R.id.frg_settings_login_btn);
        final TextView textView = (TextView) view.findViewById(R.id.frg_settings_login_txtv);
        final ImageView imageView = (ImageView) view.findViewById(R.id.frg_settings_user_imgv);

        if (MainActivity.getSpreadsheet_credential().getSelectedAccountName() != null) {
            button.setVisibility(GONE);
            textView.setText("Logged in with " + MainActivity.getSpreadsheet_credential().getSelectedAccountName());
            imageView.setImageBitmap(BitmapFactory.decodeResource(view.getResources(),
                    R.drawable.ic_verified_user_cyan_200_24dp));
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo call getResultsFromGoogleSheetsApi() from MainActivity
            }
        });

        return view;
    }
}
