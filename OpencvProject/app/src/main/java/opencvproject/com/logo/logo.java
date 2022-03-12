package opencvproject.com.logo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import opencvproject.com.R;

public class logo extends Fragment {
    private ImageView LOGO;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.logo, container,false);

        LOGO = (ImageView) v.findViewById(R.id.LOGO);
        int resId = this.getResources().getIdentifier("lcpy", "raw" , getActivity().getPackageName());
        LOGO.setImageResource(resId);

        return v;
    }
}
