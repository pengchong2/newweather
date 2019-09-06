package cn.flyaudio.weather.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.AutoCompleteTextView.OnDismissListener;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.flyaudio.base.BaseActivity;
import cn.flyaudio.base.MvpBasePresenter;
import cn.flyaudio.weather.activity.presenter.AddCityPresenter;
import cn.flyaudio.weather.activity.view.IAddCityView;
import cn.flyaudio.weather.data.WeatherWidgetApplication;
import cn.flyaudio.weather.objectInfo.CityResult;
import cn.flyaudio.weather.service.WeatherService;
import cn.flyaudio.weather.util.SPUtils;
import cn.flyaudio.weather.util.Smart_GetCity_SQL;
import cn.flyaudio.weather.util.UtilsTools;
import cn.flyaudio.weather.view.SkinResource;

public class AddCityActivity extends BaseActivity implements IAddCityView {
    private GridView grid;
    public List<String> mCityList; // 城市名称
    public List<String> mCityCodeList; // 城市代码Woeid
    private Boolean searching = false;
    private AutoCompleteTextView autoCompleteTextView;
    private TextView slideText;
    private List<CityResult> citylist = new ArrayList<CityResult>();
    private List<String> mstringArray;
    private Boolean isVisibility = false;
    int grid_page_size;
    int current_grid_page_size = 0;
    ArrayAdapter<String> adapter;
    private View cityView;
    // add for skin
    private Button btnSearch;//搜索
    private Button btnNextPage;//下一页
    private Button btnPrePage;//上一页
    private AddCityPresenter mAddCityPresenter;
    private SharedPreferences preference1;
    private SPUtils preference;

    @Override
    protected void onResume() {
        super.onResume();
        init();


        mstringArray = new ArrayList<>();
        adapter = new ArrayAdapter<String>(SkinResource.getSkinContext(), SkinResource.getSkinLayoutIdByName("gridview_cityitem"),
                mstringArray);

        grid = (GridView) findViewById(SkinResource.getSkinResourceId("commoncity", "id"));
        grid.setAdapter(adapter);


        mAddCityPresenter.getPager();
        mAddCityPresenter.getCityLiAndCityCodeLiFormSpPresenter();
    }


    @Override
    protected MvpBasePresenter getPresenter() {
        mAddCityPresenter = new AddCityPresenter();
        mAddCityPresenter.attach(this);
        return mAddCityPresenter;
    }


    private void init() {
        btnSearch = (Button) findViewById(SkinResource.getSkinResourceId("btn_search_city", "id"));
        btnNextPage = (Button) findViewById(SkinResource.getSkinResourceId("next_button", "id"));
        btnPrePage = (Button) findViewById(SkinResource.getSkinResourceId("previous_button", "id"));
        Button backButton = (Button) findViewById(SkinResource.getSkinResourceId("back", "id"));// return Button
        backButton.setOnClickListener(backClickListener);

        preference = SPUtils.getInstance();
        mCityList = new ArrayList<String>();
        mCityCodeList = new ArrayList<String>();
        slideText = (TextView) findViewById(SkinResource.getSkinResourceId("choose_city_slider_page_num", "id"));
        btnNextPage.setOnClickListener(new OnClickListener() {
            @Override //下一页按钮
            public void onClick(View v) {
                next_button_OnclickListener();
            }
        });
        btnPrePage.setOnClickListener(new OnClickListener() {
            @Override//上一页按钮
            public void onClick(View v) {
                previous_button_OnclickListener();
            }
        });
        btnSearch.setOnClickListener(new OnClickListener() {
            @Override//搜索按钮
            public void onClick(View v) {
                searchCityOnclick();
            }
        });
        cityView = findViewById(SkinResource.getSkinResourceId("citylist", "id"));
        autoCompleteTextView = (AutoCompleteTextView) findViewById(SkinResource.getSkinResourceId("autoComplete_city", "id"));

        CityAdapter mCityAdapter = new CityAdapter(null);
        autoCompleteTextView.setAdapter(mCityAdapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            //API版本大于17
            autoCompleteTextView.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss() {
                    // TODO Auto-generated method stub
                    isVisibility = false;
                    cityView.setVisibility(View.VISIBLE);
                }
            });
        }
        //自动填充按钮输入框的逻辑处理
        autoCompleteTextView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                AutioCompleteInput(position);
            }
        });
    }

    private void AutioCompleteInput(int position) {
        boolean different = true;
        CityResult result = citylist.get(position);

        String admin2 = result.getAdmin2();
        for (int i = 0; i < mCityList.size(); i++) {
            //如果相等就使用城市名，如果不相等那就用上二级的城市名加上当前的城市名。
            if (mCityList.get(i).equals((result.getCityName()/*城市名*/.equals(admin2) ? result.getCityName() :
                    (admin2 + "," + result.getCityName())))) {
                different = false;
                break;
            }
        }

        if (different == true) {
            mCityList.add(result.getCityName());
            mCityCodeList.add(result.getAreaid());
            //sp获取当前
            int current = preference.getInt("current", 1);
//                        SharedPreferences.Editor edit = preference.edit();
            preference.put("current", current);
//                        edit.commit();
            for (int j = 0; j < 4; j++) {
                String preferenceString = preference.getString(String.valueOf(j + 1), null);
                if (preferenceString == null) {
                    if (!WeatherWidgetApplication.isCNLanguage) {
                        autoCompleteTextView.setText(result.getCityname_pinyin());
                    }
                    writeSharpPreference(j + 1, (result.getCityname_pinyin().equals(result.getAdmin2_en())
                                    ? result.getCityname_pinyin() :
                                    (result.getAdmin2_en() + "," + result.getCityname_pinyin())),
                            (result.getCityName().equals(admin2) ? result.getCityName() :
                                    (admin2 + "," + result.getCityName())),
                            result.getAreaid());

                    break;
                }
            }
            startService(new Intent(AddCityActivity.this,
                    WeatherService.class));

            setResult(500);//show出刷新按钮
            finish();

        } else {
            //当API版本低于17时,监听当添加的城市已经存在的时候,显示城市列表
            if (cityView.getVisibility() == View.INVISIBLE || autoCompleteTextView.getVisibility() == View.VISIBLE) {
                isVisibility = false;
                cityView.setVisibility(View.VISIBLE);
            }
            autoCompleteTextView.setText("");
            showToast(SkinResource.getSkinStringByName("had_existed"));

        }
    }

//    }


    private OnClickListener backClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AddCityActivity.this,
                    CityEditPageActivity.class);
            startActivity(intent);
            AddCityActivity.this.finish();
        }
    };

    private void searchCityOnclick() {
        cancelToast();
        if (!UtilsTools.isNetworkAvailable(AddCityActivity.this)) {
            showToast(SkinResource.getSkinStringByName("neworkconnect"));
            return;
        }
        autoCompleteTextView.setText(autoCompleteTextView.getEditableText()
                .toString());
        searching = true;
    }

    private void previous_button_OnclickListener() {
        if (current_grid_page_size - 1 >= 0) {
            current_grid_page_size--;
            mAddCityPresenter.getPager();
        }
        adapter.notifyDataSetChanged();
        slideText.setText((current_grid_page_size + 1) + "/" + grid_page_size);
    }

    private void next_button_OnclickListener() {
        if (current_grid_page_size + 1 < grid_page_size) {
            current_grid_page_size++;
            mAddCityPresenter.getPager();
        }
        adapter.notifyDataSetChanged();
        slideText.setText((current_grid_page_size + 1) + "/" + grid_page_size);

    }


    private void writeSharpPreference(int index, String cityname_pinyin, String cityname, String citynum
    ) {
//        SharedPreferences.Editor editor = preference.edit();
        switch (index) {
            case 1:
                preference.put("1", cityname);
                preference.put("10", cityname_pinyin);
                preference.put("city1", citynum);
                break;
            case 2:
                preference.put("2", cityname);
                preference.put("20", cityname_pinyin);
                preference.put("city2", citynum);
                break;
            case 3:
                preference.put("3", cityname);
                preference.put("30", cityname_pinyin);
                preference.put("city3", citynum);
                break;
            case 4:
                preference.put("4", cityname);
                preference.put("40", cityname_pinyin);
                preference.put("city4", citynum);
                break;
            default:
                break;
        }
    }

    @Override
    protected int getlayoutId() {
        int initPreloadViewsID = this.getResources()
                .getIdentifier("addcity_activity_layout", "layout", this.getPackageName());
        return initPreloadViewsID;
    }

    @Override
    protected void initView() {

    }


    public void showData(final List<String> mStringArray, final String[] cityname) {
        grid_page_size = (int) Math.ceil(cityname.length / (12 * 1.0));
        slideText.setText((current_grid_page_size + 1) + "/" + grid_page_size);

        grid.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long arg3) {
                autoCompleteTextView.setText(cityname[position
                        + current_grid_page_size * 12]);
                searchCityOnclick();
            }
        });
        mstringArray.clear();
        mstringArray.addAll(mStringArray);
        adapter.notifyDataSetChanged();

    }


    @Override
    public int getPager() {
        return current_grid_page_size;
    }

    @Override
    public void getCityCodeList(List<String> cityList, List<String> mCityList, List<String> mCityEnList) {
        this.mCityCodeList = mCityList;
        this.mCityList = cityList;
    }

    private class CityAdapter extends ArrayAdapter<CityResult> implements
            Filterable {

        public CityAdapter(List<CityResult> citylist) {
            super(SkinResource.getSkinContext(), SkinResource.getSkinLayoutIdByName("cityresult_layout"), citylist);
        }

        @Override
        public int getCount() {
            if (citylist != null) {
                return citylist.size();
            }
            return 0;
        }

        @Override
        public CityResult getItem(int position) {

            if (citylist != null) {
                return citylist.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (citylist != null) {
                return citylist.get(position).hashCode();
            }
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater layoutInflater = (LayoutInflater) SkinResource.getSkinContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(SkinResource.getSkinLayoutIdByName("cityresult_layout"),
                        parent, false);
            }
            if (citylist != null) {
                String cityName;
                String adminName;
                String admin2Name;
                String countryName;

                TextView tvTextView = (TextView) view
                        .findViewById(SkinResource.getSkinResourceId("cityitem", "id"));

                if (WeatherWidgetApplication.isCNLanguage) {
                    cityName = citylist.get(position).getCityName();
                    admin2Name = citylist.get(position).getAdmin2();
                    adminName = citylist.get(position).getAdmin1();
                } else {
                    cityName = citylist.get(position).getCityname_pinyin();
                    admin2Name = citylist.get(position).getAdmin2_en();
                    adminName = WeatherWidgetApplication.toUpperCaseFirstOne(citylist.get(position).getAdmin1_en());
                }

                countryName = citylist.get(position).getCountry();

                tvTextView.setText((adminName != null ? (adminName + ",") : "") +
                        (admin2Name != null ? (admin2Name + ",") : "") +
                        (cityName != null ? (cityName + "") : ""));
            }
            return view;
        }

        @Override //获取过滤器
        public Filter getFilter() {
            Filter cityFilter = new Filter() {//创建filter
                @Override
                protected void publishResults(CharSequence contain,
                                              FilterResults results) {
                    if (isVisibility) {
                        cityView.setVisibility(View.INVISIBLE);
                    }
                    citylist = (List<CityResult>) results.values;
                    notifyDataSetChanged();
                }

                @Override
                protected FilterResults performFiltering(CharSequence contait) {
                    FilterResults results = new FilterResults();

                    if (contait == null || contait.length() < 1) {
                        return results;
                    }
                    if (searching == true) {
                        List<CityResult> citylist;
                        citylist =  Smart_GetCity_SQL.getHelper(getApplicationContext()).query(contait.toString());
                        results.values = citylist;
                        results.count = citylist.size();
                        if (results.count > 0) {
                            isVisibility = true;
                        } else {
                            searching = false;
                            //showToast(getResources().getString(R.string.nullsearch));
                        }
                        searching = false;
                    }
                    return results;
                }
            };
            return cityFilter;
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {//点击的是返回键
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {//按键的按下事件

                AddCityActivity.this.finish();

            }
        }
        return super.dispatchKeyEvent(event);
    }
}