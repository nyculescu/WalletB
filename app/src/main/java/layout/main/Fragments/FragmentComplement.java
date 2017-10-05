package layout.main.Fragments;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import nyc.walletb.R;

public class FragmentComplement extends Fragment implements FragmentComplementInterface {
    protected final String TAG = getClass().getName();

    FrameLayout fragmentContainer = null;

    @Override
    public void init(View view, @IdRes int id_FrameLayout) {
        fragmentContainer = (FrameLayout) view.findViewById(id_FrameLayout);
    }

    /**
     * Refresh fragment
     */
    @Override
    public void refresh() {
        //tbd
    }

    /**
     * Called when a fragment will be displayed - animation fade in of the current Fragment
     */
    @Override
    public void willBeDisplayed(Context context) {
        // Do what you want here, for example animate the content
        if (fragmentContainer != null) {
            Animation fadeIn = AnimationUtils.loadAnimation(context, R.anim.botnavi_fade_in);
            fragmentContainer.startAnimation(fadeIn);
        }
    }

    /**
     * Called when a fragment will be hidden - animation fade out of the current Fragment
     */
    @Override
    public void willBeHidden(Context context) {
        if (fragmentContainer != null) {
            Animation fadeOut = AnimationUtils.loadAnimation(context, R.anim.botnavi_fade_out);
            fragmentContainer.startAnimation(fadeOut);
        }
    }
}
