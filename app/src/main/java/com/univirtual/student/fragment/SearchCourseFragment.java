package com.univirtual.student.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.univirtual.student.R;
import com.univirtual.student.activity.MyListActivity;
import com.univirtual.student.activity.SearchCourseActivity;
import com.univirtual.student.adapter.ListAdapter;
import com.univirtual.student.other.InitApplication;
import com.univirtual.student.realm.RealmBanner;
import com.univirtual.student.util.RealmUtility;
import com.univirtual.student.util.carousel.ViewPagerCarouselView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.univirtual.student.activity.HomeActivity.APITOKEN;
import static com.univirtual.student.activity.HomeActivity.homeactivity;
import static com.univirtual.student.constants.keyConst.INSTRUCTOR_API_BASE_URL;


public class SearchCourseFragment extends Fragment {
    ArrayList<String> titles;
    ArrayList<RealmBanner> realmBannerArrayList;
    RecyclerView recyclerView;
    private ShimmerFrameLayout shimmer_view_container;

    static ViewPagerCarouselView viewPagerCarouselView;
    public static RelativeLayout searchlayout;
    public static LinearLayout error_loading;
    Button retrybtn;
    FrameLayout frame;
    Activity context;

    public SearchCourseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_search_course, container, false);

        context = getActivity();
        ArrayList<String> titles = new ArrayList<String>() {{
            add(context.getString(R.string.preschool));
            add(getString(R.string.primary_school));
            add(context.getString(R.string.jhs));
            add(context.getString(R.string.shs));
            add(context.getString(R.string.preuniversity));
            add(context.getString(R.string.university));
            add(context.getString(R.string.professional));
            add(context.getString(R.string.vocational));
        }};

        viewPagerCarouselView = rootView.findViewById(R.id.carousel_view);
        frame = rootView.findViewById(R.id.frame);
        searchlayout = rootView.findViewById(R.id.searchlayout);

        searchlayout.setOnClickListener(view -> startActivity(new Intent(getContext(), SearchCourseActivity.class)));
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ListAdapter listAdapter = new ListAdapter((names, position, holder) -> {
            String textViewText = names.get(position);
            startActivity(new Intent(getContext(), MyListActivity.class).putExtra("title", textViewText));

        }, getActivity(), titles, "");

        shimmer_view_container = rootView.findViewById(R.id.shimmer_view_container);
        shimmer_view_container.startShimmerAnimation();
        error_loading = rootView.findViewById(R.id.error_loading);


        recyclerView.setAdapter(listAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getBanners();
    }

    public void getBanners() {
        String URL = null;
        URL = INSTRUCTOR_API_BASE_URL + "getbanners.php";
        realmBannerArrayList = new ArrayList<>();
        Realm.init(getContext());
        String finalURL = URL;
        try {
            shimmer_view_container.startShimmerAnimation();
            shimmer_view_container.setVisibility(View.VISIBLE);
            frame.setVisibility(View.GONE);
//                    error_loading.setVisibility(View.GONE);
            //mShimmerViewContainer.setVisibility(View.GONE);
            JsonObjectRequest jsojsonObjectRequestOblect = new JsonObjectRequest(Request.Method.GET, finalURL, null, response -> {
                if (response != null) {
                    Realm.init(getContext());
                    Realm.getInstance(RealmUtility.getDefaultConfig(homeactivity)).executeTransaction(realm -> {
                        try {
                            RealmResults<RealmBanner> result = realm.where(RealmBanner.class).findAll();
                            result.deleteAllFromRealm();

                            realm.createOrUpdateAllFromJson(RealmBanner.class, response.getJSONArray("courseinfo"));
                            shimmer_view_container.stopShimmerAnimation();
                            shimmer_view_container.setVisibility(View.GONE);
//                                error_loading.setVisibility(View.VISIBLE);
                            RealmResults<RealmBanner> realmBanners = realm.where(RealmBanner.class).findAll();
                            for (RealmBanner banner : realmBanners) {
                                realmBannerArrayList.add(banner);
                            }
                            if (realmBannerArrayList.size() > 0) {
                                viewPagerCarouselView.setData(getFragmentManager(), realmBannerArrayList, 3500);
                                frame.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }, error -> {
                shimmer_view_container.stopShimmerAnimation();
                shimmer_view_container.setVisibility(View.GONE);


                Realm.init(getContext());
                Realm.getInstance(RealmUtility.getDefaultConfig(homeactivity)).executeTransaction(realm -> {
                    RealmResults<RealmBanner> realmBannerRealmResults = realm.where(RealmBanner.class).findAll();
                    if (realmBannerRealmResults.size() > 0) {

                        for (RealmBanner banner : realmBannerRealmResults) {
                            realmBannerArrayList.add(banner);
                        }
                        viewPagerCarouselView.setData(getFragmentManager(), realmBannerArrayList, 3500);
                        frame.setVisibility(View.VISIBLE);
                        //  error_loading.setVisibility(View.GONE);

                    } else {
                        ///  error_loading.setVisibility(View.VISIBLE);
                    }
                });
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    final Map<String, String> headers = new HashMap<>();
                    headers.put("Accept", "application/json");
                    headers.put("Authorization", "Bearer " + PreferenceManager.getDefaultSharedPreferences(context).getString(APITOKEN, ""));

                    return headers;
                }
            };
            jsojsonObjectRequestOblect.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            InitApplication.getInstance().addToRequestQueue(jsojsonObjectRequestOblect);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
