package layout.main.Fragments;

import android.content.Context;
import android.support.annotation.IdRes;
import android.view.View;

public interface FragmentComplementInterface {
    void init(View view, @IdRes int id_FrameLayout);

    void refresh();

    void willBeDisplayed(Context context);

    void willBeHidden(Context context);
}
